const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const firebase = require("/usr/local/lib/node_modules/@firebase/rules-unit-testing/node_modules/firebase/compat/app").default;
require("/usr/local/lib/node_modules/@firebase/rules-unit-testing/node_modules/firebase/compat/firestore");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const EVE_UID = "eve_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function serverTs(_db) {
  return firebase.firestore.FieldValue.serverTimestamp();
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated caller: cannot read posts, stories, music_tracks, or users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("posts").limit(10).get());
  await assertFails(unauthDb.collection("stories").limit(10).get());
  await assertFails(unauthDb.collection("music_tracks").limit(10).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated caller: cannot list posts without pagination limit (anti-scraping)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("posts").orderBy("createdAt", "desc").get());
  await assertFails(aliceDb.collection("posts").orderBy("createdAt", "desc").limit(100).get());
  await assertSucceeds(aliceDb.collection("posts").orderBy("createdAt", "desc").limit(30).get());
});

test("PII Isolation: Bob cannot read Alice's private user doc, but can read public_profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = serverTs(aliceDb);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Alice",
      username: "alice",
      email: "alice@example.com",
      createdAt: 1700000000000,
      updatedAt: ts,
    })
  );
  await assertSucceeds(
    aliceDb.collection("public_profiles").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Alice",
      username: "alice",
      createdAt: 1700000000000,
      updatedAt: ts,
    })
  );

  // Bob CANNOT read private user PII
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  // Bob CANNOT write PII into public_profiles
  await assertFails(
    bobDb.collection("public_profiles").doc(BOB_UID).set({
      userId: BOB_UID,
      fullName: "Bob",
      username: "bob",
      email: "bob@example.com",
      createdAt: 1700000000000,
      updatedAt: serverTs(bobDb),
    })
  );
  // Bob CAN read Alice's non-PII public profile
  await assertSucceeds(bobDb.collection("public_profiles").doc(ALICE_UID).get());
});

test("Posts + Cloud Reactions + Comment Replies + Cross-User Tamper Prevention", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const tsA = serverTs(aliceDb);
  const tsB = serverTs(bobDb);

  // Alice creates post
  await assertSucceeds(
    aliceDb.collection("posts").doc("post_1").set({
      userId: ALICE_UID,
      authorId: ALICE_UID,
      userName: "Alice",
      text: "Hello FotoFilm!",
      reactionCounts: { LIKE: 1 },
      shareCount: 0,
      commentCount: 0,
      createdAt: 1700000001000,
      updatedAt: tsA,
    })
  );

  // Bob CANNOT spoof Alice as author
  await assertFails(
    bobDb.collection("posts").doc("post_spoof").set({
      userId: ALICE_UID,
      authorId: ALICE_UID,
      userName: "Alice",
      text: "Spoofed post",
      createdAt: 1700000002000,
      updatedAt: tsB,
    })
  );

  // Bob CANNOT edit Alice's post text
  await assertFails(
    bobDb.collection("posts").doc("post_1").update({
      text: "Hacked!",
      updatedAt: tsB,
    })
  );

  // Bob CAN react to Alice's post (per-user reaction doc + counter increment)
  await assertSucceeds(
    bobDb.collection("posts").doc("post_1").collection("reactions").doc(BOB_UID).set({
      userId: BOB_UID,
      reactionType: "LOVE",
      userName: "Bob",
      updatedAt: tsB,
    })
  );
  await assertSucceeds(
    bobDb.collection("posts").doc("post_1").update({
      reactionCounts: { LIKE: 1, LOVE: 1 },
      shareCount: 1,
      updatedAt: tsB,
    })
  );

  // Bob CANNOT overwrite Alice's reaction doc
  await assertFails(
    bobDb.collection("posts").doc("post_1").collection("reactions").doc(ALICE_UID).set({
      userId: ALICE_UID,
      reactionType: "ANGRY",
      updatedAt: tsB,
    })
  );

  // Bob adds comment on Alice's post
  await assertSucceeds(
    bobDb.collection("posts").doc("post_1").collection("comments").doc("c_1").set({
      postId: "post_1",
      authorId: BOB_UID,
      authorFullName: "Bob",
      text: "Nice post!",
      reactionCounts: {},
      createdAt: 1700000003000,
      updatedAt: tsB,
    })
  );

  // Alice replies to Bob's comment and updates reply reaction
  await assertSucceeds(
    aliceDb.collection("posts").doc("post_1").collection("comments").doc("c_1").collection("replies").doc("cr_1").set({
      postId: "post_1",
      commentId: "c_1",
      authorId: ALICE_UID,
      authorFullName: "Alice",
      text: "Thanks Bob!",
      reactionCounts: {},
      createdAt: 1700000004000,
      updatedAt: tsA,
    })
  );

  // Bob reacts to Alice's reply
  await assertSucceeds(
    bobDb.collection("posts").doc("post_1").collection("comments").doc("c_1").collection("replies").doc("cr_1")
      .collection("reactions").doc(BOB_UID).set({
        userId: BOB_UID,
        reactionType: "HUNDRED",
        updatedAt: tsB,
      })
  );
  await assertSucceeds(
    bobDb.collection("posts").doc("post_1").collection("comments").doc("c_1").collection("replies").doc("cr_1").update({
      reactionCounts: { HUNDRED: 1 },
      updatedAt: tsB,
    })
  );

  // Bob CANNOT delete Alice's post
  await assertFails(bobDb.collection("posts").doc("post_1").delete());
  // Alice CAN delete her own post
  await assertSucceeds(aliceDb.collection("posts").doc("post_1").delete());
});

test("ChatFilm Conversations & Messages: strict participant isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const eveDb = testEnv.authenticatedContext(EVE_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("conversations").doc("dm_alice_bob").set({
      createdBy: ALICE_UID,
      participantIds: [ALICE_UID, BOB_UID],
      isGroup: false,
      lastMessage: "Hi Bob",
      createdAt: 1700000005000,
      updatedAt: serverTs(aliceDb),
    })
  );

  await assertSucceeds(
    aliceDb.collection("conversations").doc("dm_alice_bob").collection("messages").doc("msg_1").set({
      conversationId: "dm_alice_bob",
      senderId: ALICE_UID,
      senderName: "Alice",
      participantIds: [ALICE_UID, BOB_UID],
      text: "Salam!",
      createdAt: 1700000006000,
      updatedAt: serverTs(aliceDb),
    })
  );

  // Bob CAN read conversation & messages matching participantIds filter
  await assertSucceeds(bobDb.collection("conversations").doc("dm_alice_bob").get());
  await assertSucceeds(
    bobDb.collection("conversations").doc("dm_alice_bob").collection("messages")
      .where("participantIds", "array-contains", BOB_UID)
      .limit(50)
      .get()
  );

  // Eve CANNOT read Alice & Bob's conversation or list their messages
  await assertFails(eveDb.collection("conversations").doc("dm_alice_bob").get());
  await assertFails(
    eveDb.collection("conversations").doc("dm_alice_bob").collection("messages").limit(50).get()
  );
  await assertFails(
    eveDb.collection("conversations").doc("dm_alice_bob").collection("messages")
      .where("participantIds", "array-contains", BOB_UID)
      .limit(50)
      .get()
  );
});

test("Ludo Rooms: terminal FINISHED state prevents further tampering", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("ludo_rooms").doc("room_1").set({
      roomId: "room_1",
      hostId: ALICE_UID,
      hostName: "Alice",
      status: "WAITING",
      turnColor: "BLUE",
      diceValue: 1,
      isDiceRolled: false,
      players: [{ id: ALICE_UID, name: "Alice" }],
      createdAt: 1700000007000,
      updatedAt: serverTs(aliceDb),
    })
  );

  await assertSucceeds(
    bobDb.collection("ludo_rooms").doc("room_1").update({
      status: "FINISHED",
      updatedAt: serverTs(bobDb),
    })
  );

  // Once FINISHED, even Alice cannot update dice/turn
  await assertFails(
    aliceDb.collection("ludo_rooms").doc("room_1").update({
      diceValue: 6,
      updatedAt: serverTs(aliceDb),
    })
  );
});
