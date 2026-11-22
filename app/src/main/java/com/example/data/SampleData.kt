package com.example.data

import com.example.model.*

object SampleData {
    val currentUser = User(
        id = "user_me",
        fullName = "Niyon Islam Sohag",
        username = "niyon_sohag",
        email = "smd224292@gmail.com",
        avatarUrl = "",
        coverUrl = "",
        bio = "অনবরত মা মাটি মানুষের জন্য🌱\nAlhamdulillah for everything🤲",
        isVerified = true,
        isOnline = true,
        lastSeenMinutesAgo = 0,
        friendCount = 0,
        followerCount = 0,
        followingCount = 0,
        postCount = 0,
        note = "Share a note...",
        gender = "পুরুষ",
        country = "বাংলাদেশ 🇧🇩",
        birthDate = "01/05/2004",
        category = "Digital creator",
        workplace = "Digital creator",
        livesIn = "Dhaka, Bangladesh",
        fromCity = "Dhaka, Bangladesh",
        school = "লাউপাডা সাগর সৈকত মাধ্যমিক বিদ্যালয়",
        college = "Taltali Degree College .তালতলী ডিগ্রী কলেজ",
        instagramHandle = "niyonislamsohag",
        website = "niyonislamsohag.com",
        joinedDate = "Joined May 2020",
        relationshipStatus = "Single"
    )

    // Strictly real users & Cloudinary media — zero demo users/photos/videos
    val pendingRequestUsers: List<User> = emptyList()
    val suggestedUsers: List<User> = emptyList()
    val profileFriends: List<ProfileFriend> = emptyList()
    val initialStories: List<Story> = emptyList()
    val tenSamplePhotos: List<String> = emptyList()
    val initialPosts: List<Post> = emptyList()
    val allAppUsersList: List<User> = listOf(currentUser)
    val initialConversations: List<Conversation> = emptyList()
    val initialNotifications: List<AppNotification> = emptyList()
    val initialGroups: List<SocialGroup> = emptyList()
    val initialPages: List<SocialPage> = emptyList()
    val initialFollowers: List<FollowerUser> = emptyList()
    val initialFollowing: List<FollowerUser> = emptyList()
    val galleryMediaItems: List<GalleryMediaItem> = emptyList()
    val sampleSocialAds: List<com.example.model.SocialAd> = emptyList()
    val sampleLiveComments: List<LiveCommentItem> = emptyList()

    // Music & Audio Tracks Catalog for Posts (metadata only, no demo stock cover images)
    val sampleAudioTracks: List<PostAudioTrack> = listOf(
        // গজল (Islamic Ghazal)
        PostAudioTrack("aud_1", "হাসবি রাব্বি জাল্লাল্লাহ (হৃদয় জুড়ানো কাওয়ালি)", "সামি ইউসুফ ও বাংলা শিল্পীগোষ্ঠী", "গজল", "৪:১২", coverArtUrl = null),
        PostAudioTrack("aud_2", "ইয়া নবী সালাম আলাইকা (মধুর কণ্ঠে গজল)", "আন্তর্জাতিক ইসলামিক নাশিদ", "গজল", "৩:৪৫", coverArtUrl = null),
        PostAudioTrack("aud_3", "আল্লাহু আল্লাহু তুমি জাল্লে শানহু", "বাংলা সুফি সংগীত", "গজল", "৩:২০", coverArtUrl = null),
        PostAudioTrack("aud_4", "তুমি রহমান তুমি মেহেরবান", "ইসলামিক হামদ ও নাত", "গজল", "৪:৩০", coverArtUrl = null),

        // বাঁশির সুর (Soulful Flute Melodies)
        PostAudioTrack("aud_5", "পল্লী সুর - দূর গাঁয়ের মায়াবী বাঁশির তান", "ওস্তাদ আলাউদ্দিন খাঁ স্মৃতি বাদক", "বাঁশির সুর", "২:৫০", coverArtUrl = null),
        PostAudioTrack("aud_6", "ভাটিয়ালি বাঁশির সুর - নদীর কূলে সন্ধ্যার গান", "বাংলার লোক সুরকার", "বাঁশির সুর", "৩:১৫", coverArtUrl = null),
        PostAudioTrack("aud_7", "শান্ত স্নিগ্ধ সকালের বাঁশির মধুর ধুন", "মেডিটেশন ও নেচার মিউজিক", "বাঁশির সুর", "৩:৪০", coverArtUrl = null),
        PostAudioTrack("aud_8", "মেঘলা দিনের বিষাদ বাঁশি ও বৃষ্টির শব্দ", "মন কেমন করা সুর", "বাঁশির সুর", "২:৩৫", coverArtUrl = null),

        // জেমস ও আইয়ুব বাচ্চু
        PostAudioTrack("aud_9", "বাংলাদেশ - এই দূর পরবাসে তারা ভরা রাতে", "নগর বাউল জেমস (James)", "জেমস ও আইয়ুব বাচ্চু", "৫:১০", coverArtUrl = null),
        PostAudioTrack("aud_10", "সেই তুমি কেন এতো অচেনা হলে", "আইয়ুব বাচ্চু (LRB)", "জেমস ও আইয়ুব বাচ্চু", "৪:৫৫", coverArtUrl = null),
        PostAudioTrack("aud_11", "কবিতা তুমি স্বপ্নচারিণী হয়ে খবর নিও না", "নগর বাউল জেমস (James)", "জেমস ও আইয়ুব বাচ্চু", "৪:১৮", coverArtUrl = null),
        PostAudioTrack("aud_12", "কষ্ট পেতে ভালোবাসি তাই তোমার কাছে ছুটে আসি", "আইয়ুব বাচ্চু (LRB)", "জেমস ও আইয়ুব বাচ্চু", "৪:৪০", coverArtUrl = null),
        PostAudioTrack("aud_13", "পদ্মা পাতার জল - জেমস", "নগর বাউল জেমস", "জেমস ও আইয়ুব বাচ্চু", "৩:৫০", coverArtUrl = null),
        PostAudioTrack("aud_14", "তারা ভরা রাতে তোমার কথা মনে পড়ে", "আইয়ুব বাচ্চু (LRB)", "জেমস ও আইয়ুব বাচ্চু", "৫:০২", coverArtUrl = null),

        // ডিজে ও বিট মিউজিক
        PostAudioTrack("aud_15", "ডিজে বাংলা বাউন্স ক্লাব রিমিক্স ২০২৬", "ডিজে রাহাত ও ফ্রেন্ডস", "ডিজে গান", "৩:৩০", coverArtUrl = null),
        PostAudioTrack("aud_16", "বাংলা ফোক ডিজে ধামাকা পার্টি মিক্স", "ডিজে সনিক বিডি", "ডিজে গান", "৩:১৫", coverArtUrl = null),
        PostAudioTrack("aud_17", "বাস বুস্টার ইলেকট্রনিক ড্যান্স ড্রপ বিট", "বিডি ইডিএম লাভার্স", "ডিজে গান", "২:৪৫", coverArtUrl = null),

        // হিন্দি গান
        PostAudioTrack("aud_18", "তুম হি হো (Tum Hi Ho - Aashiqui 2)", "অরিজিৎ সিং (Arijit Singh)", "হিন্দি গান", "৪:২২", coverArtUrl = null),
        PostAudioTrack("aud_19", "কেসারিয়া তেরা ইশক হ্যায় পিয়া (Kesariya)", "অরিজিৎ সিং ও প্রীতম", "হিন্দি গান", "৪:২৮", coverArtUrl = null),
        PostAudioTrack("aud_20", "তেরে ওয়াস্তে ফলক সে মে চাঁদ লাউঙ্গা", "শচীন-জিগার ও বরুণ জৈন", "হিন্দি গান", "৩:০৯", coverArtUrl = null),

        // দেশাত্মবোধক গান
        PostAudioTrack("aud_21", "আমার সোনার বাংলা আমি তোমায় ভালোবাসি", "জাতীয় সংগীত বাংলাদেশ 🇧🇩", "দেশাত্মবোধক", "৩:০০", coverArtUrl = null),
        PostAudioTrack("aud_22", "ধনধান্য পুষ্পভরা আমাদের এই বসুন্ধরা", "দ্বিজেন্দ্রলাল রায় - দেশাত্মবোধক", "দেশাত্মবোধক", "৩:৩০", coverArtUrl = null),
        PostAudioTrack("aud_23", "এক সাগর রক্তের বিনিময়ে বাংলার স্বাধীনতা", "স্বাধীন বাংলা বেতার কেন্দ্র", "দেশাত্মবোধক", "৩:৪০", coverArtUrl = null),
        PostAudioTrack("aud_24", "তীর হারা এই ঢেউয়ের সাগর পাড়ি দেব রে", "আপেল মাহমুদ - বীর মুক্তিযোদ্ধার গান", "দেশাত্মবোধক", "৩:১৫", coverArtUrl = null),

        // লালনগীতি ও বাউল
        PostAudioTrack("aud_25", "মিলন হবে কত দিনে আমার মনের মানুষের সনে", "ফকির লালন শাহ্ স্মৃতি সুর", "লালনগীতি", "৫:২০", coverArtUrl = null),
        PostAudioTrack("aud_26", "খাঁচার ভিতর অচিন পাখি কেমনে আসে যায়", "ফরিদা পারভীন - লালন সুর", "লালনগীতি", "৪:১৫", coverArtUrl = null),
        PostAudioTrack("aud_27", "জাত গেল জাত গেল বলে একি আজব কারখানা", "বাউল সম্রাট লালনগীতি", "লালনগীতি", "৪:৪০", coverArtUrl = null),
        PostAudioTrack("aud_28", "সব লোকে কয় লালন কি জাত সংসারে", "বাংলার বাউল আখড়া", "লালনগীতি", "৩:৫০", coverArtUrl = null),

        // রবীন্দ্র সংগীত
        PostAudioTrack("aud_29", "পুরানো সেই দিনের কথা ভুলবি কি রে হায়", "বিশ্বকবি রবীন্দ্রনাথ ঠাকুর", "রবীন্দ্র সংগীত", "৩:১০", coverArtUrl = null),
        PostAudioTrack("aud_30", "তুমি রবে নীরবে হৃদয়ে মম", "রবীন্দ্র ক্লাসিক সুর", "রবীন্দ্র সংগীত", "৪:০৫", coverArtUrl = null),
        PostAudioTrack("aud_31", "যদি তোর ডাক শুনে কেউ না আসে তবে একলা চলো রে", "বিশ্বকবির অমর অনুপ্রেরণা", "রবীন্দ্র সংগীত", "৩:২৫", coverArtUrl = null),
        PostAudioTrack("aud_32", "আমার পরান যাহা চায় তুমি তাই", "হৃদয়ছোঁয়া রবীন্দ্র সংগীত", "রবীন্দ্র সংগীত", "৩:৫০", coverArtUrl = null),

        // দুঃখের ও আবেগঘন সুর
        PostAudioTrack("aud_33", "একাকী নিঃসঙ্গ রাতের দীর্ঘশ্বাস - দুঃখের বাঁশি ও সুর", "স্যাড মেলোডি ইন্সট্রুমেন্টাল", "দুঃখের গান", "৩:৪০", coverArtUrl = null),
        PostAudioTrack("aud_34", "যে পাখি ঘর বোঝে না উড়াল দিল আকাশে", "বিরহের গভীর তান", "দুঃখের গান", "৪:১২", coverArtUrl = null),
        PostAudioTrack("aud_35", "হারিয়ে যাওয়া পুরনো দিন আর না বলা কথা", "স্মৃতির পাতা থেকে", "দুঃখের গান", "৩:২৫", coverArtUrl = null),
        PostAudioTrack("aud_36", "অভিমানী চোখের জল মুছে দেওয়ার কেউ নেই", "স্যাড ক্লাসিক ট্র্যাক", "দুঃখের গান", "৩:৫০", coverArtUrl = null),

        // জন্মদিনের গান
        PostAudioTrack("aud_bday_1", "শুভ জন্মদিন প্রিয় বন্ধু (Happy Birthday To You 🎂)", "বাংলা সেলিব্রেশন শিল্পীগোষ্ঠী", "শুভ জন্মদিন", "৩:১৫", coverArtUrl = null),
        PostAudioTrack("aud_bday_2", "আজ তোমার জন্মদিন - আলোর রোশনাই ছড়িয়ে যাক 🎉", "সুর সম্রাট ও বার্থডে অর্কেস্ট্রা", "শুভ জন্মদিন", "৩:৪০", coverArtUrl = null),
        PostAudioTrack("aud_bday_3", "বার্থডে ডিজে পার্টি ধামাকা মিক্স ২০২৬ 🎈🥳", "ডিজে পার্টি লাভার্স বিডি", "শুভ জন্মদিন", "২:৫০", coverArtUrl = null),
        PostAudioTrack("aud_bday_4", "ছোট্ট সোনামণির শুভ জন্মদিন (Sweet Birthday Kids 🍰)", "শিশু কিশোর কণ্ঠশিল্পীরা", "শুভ জন্মদিন", "২:৪৫", coverArtUrl = null),
        PostAudioTrack("aud_bday_5", "Happy Birthday To You (Acoustic Guitar & Piano 🎁)", "গ্লোবাল অ্যাকোস্টিক মেলোডি", "শুভ জন্মদিন", "৩:২০", coverArtUrl = null),
        PostAudioTrack("aud_bday_6", "শুভ শুভ শুভ জন্মদিন - শত বছর বেঁচে থাকো তুমি 🕯️", "হৃদয়ছোঁয়া জন্মদিনের আশীর্বাদ সুর", "শুভ জন্মদিন", "৩:৩০", coverArtUrl = null),
        PostAudioTrack("aud_bday_7", "বার্থডে কেক কাটার মধুর সুর ও বাঁশি 🍰🎶", "মেলোডি ইন্সট্রুমেন্টাল", "শুভ জন্মদিন", "৩:০৫", coverArtUrl = null),
        PostAudioTrack("aud_bday_8", "বার্থডে রিমিক্স ২০২৬ - পার্টি নাইট সেলিব্রেশন 🎊", "ডিজে নাইটস ঢাকা", "শুভ জন্মদিন", "৩:১৫", coverArtUrl = null),

        // অডিও গান
        PostAudioTrack("aud_song_1", "তুমি আমার ঘুম ভাঙ্গানিয়া গান", "বাংলা রোমান্টিক ক্লাসিক", "অডিও গান", "৪:১৫", coverArtUrl = null),
        PostAudioTrack("aud_song_2", "মেঘ বলেছে যাব যাব - ছায়াসুন্দরী সুর", "আধুনিক বাংলা মেলোডি", "অডিও গান", "৩:৪৮", coverArtUrl = null),
        PostAudioTrack("aud_song_3", "ওরে নীল দরিয়া আমায় দে রে দে ছাড়িয়া", "অমর বাংলা লোকগীতি", "অডিও গান", "৪:০৫", coverArtUrl = null),
        PostAudioTrack("aud_song_4", "এই সুন্দর স্বর্ণালী সন্ধ্যায় একাকী বসে", "হারানো দিনের সোনালী গান", "অডিও গান", "৩:৩৫", coverArtUrl = null),

        // মিউজিক
        PostAudioTrack("aud_music_1", "শান্ত স্নিগ্ধ পিয়ানো ও অ্যাকোস্টিক গিটার মিউজিক", "রিল্যাক্সিং সাউন্ডস বিডি", "মিউজিক", "৩:৪০", coverArtUrl = null),
        PostAudioTrack("aud_music_2", "বাংলার পল্লী মেঠো সুর ও শান্ত ব্যাকগ্রাউন্ড মিউজিক (BGM)", "ফোক অর্কেস্ট্রা ঢাকা", "মিউজিক", "৪:১২", coverArtUrl = null),
        PostAudioTrack("aud_music_3", "সিনেমাটিক রোমান্টিক ভায়োলিন ও সিন্থ মিউজিক", "সুর সাগর স্টুডিও", "মিউজিক", "৩:২৫", coverArtUrl = null),
        PostAudioTrack("aud_music_4", "ক্যাফে অ্যাকোস্টিক বিজিএম - মন জুড়ানো সুর", "বিডি অ্যাম্বিয়েন্স মিউজিক", "মিউজিক", "২:৫৫", coverArtUrl = null),

        // জন্মদিন গান
        PostAudioTrack("aud_bday_9", "শুভ জন্মদিন গান - শত জনম বেঁচে রও তুমি 🎂", "বার্থডে সুর শিল্পীগোষ্ঠী", "জন্মদিন গান", "৩:২০", coverArtUrl = null),
        PostAudioTrack("aud_bday_10", "আজ শুভ জন্মদিন তোমার - আলো ছড়াও ভুবনে 🎈", "বার্থডে মেলোডি কালেকশন", "জন্মদিন গান", "৩:১৫", coverArtUrl = null),
        PostAudioTrack("aud_bday_11", "Happy Birthday Beat Mix ২০২৬ 🥳🎉", "বার্থডে পার্টি ডিজে", "জন্মদিন গান", "২:৫০", coverArtUrl = null)
    )

    // জন্মদিনের স্পেশাল ইমুজি কালেকশন
    val birthdayEmojis: List<String> = listOf(
        "🎂", "🎉", "🎈", "🥳", "🎁", "🕯️", "🍰", "🎊",
        "🧁", "🎇", "💖", "🥂", "👑", "💐", "🌟", "🎶"
    )

    // 10 Admin Slots for Right Manager Fida (Slot 1 = Owner Niyon Islam Sohag, remaining 9 slots empty until assigned)
    val initialAdminSlots: List<AdminSlot> = listOf(
        AdminSlot(1, currentUser, "প্রধান প্রতিষ্ঠাতা ও সুপার অ্যাডমিন 👑", isSuperAdmin = true, assignedDate = "০১/০১/২০২৬ (1/1/2026)"),
        AdminSlot(2, null, "অ্যাডমিন পদ খালি (ফাঁকা)", isSuperAdmin = false),
        AdminSlot(3, null, "অ্যাডমিন পদ খালি (ফাঁকা)", isSuperAdmin = false),
        AdminSlot(4, null, "অ্যাডমিন পদ খালি (ফাঁকা)", isSuperAdmin = false),
        AdminSlot(5, null, "অ্যাডমিন পদ খালি (ফাঁকা)", isSuperAdmin = false),
        AdminSlot(6, null, "অ্যাডমিন পদ খালি (ফাঁকা)", isSuperAdmin = false),
        AdminSlot(7, null, "অ্যাডমিন পদ খালি (ফাঁকা)", isSuperAdmin = false),
        AdminSlot(8, null, "অ্যাডমিন পদ খালি (ফাঁকা)", isSuperAdmin = false),
        AdminSlot(9, null, "অ্যাডমিন পদ খালি (ফাঁকা)", isSuperAdmin = false),
        AdminSlot(10, null, "অ্যাডমিন পদ খালি (ফাঁকা)", isSuperAdmin = false)
    )

    // Empty initial state for all Right Manager Fida audit/history/ticket lists (real runtime data only)
    val initialCyberOffenders: List<CyberOffenderReport> = emptyList()
    val initialAppUpdateBroadcasts: List<AppUpdateBroadcast> = emptyList()
    val initialFreeVerifiedGrantRecords: List<FreeVerifiedGrantRecord> = emptyList()
    val initialUserVerificationAuditEntries: List<UserVerificationAuditEntry> = emptyList()
    val initialHighlightsMap: Map<String, List<StoryHighlight>> = emptyMap()
    val initialTimedSuspensions: List<UserSuspensionRecord> = emptyList()
    val sampleSupportTickets: List<SupportTicket> = emptyList()
    val initialUserAdAnalytics: List<UserAdAnalytics> = emptyList()
    val initialFlaggedMessengerAudits: List<com.example.model.FlaggedMessengerAudit> = emptyList()
}
