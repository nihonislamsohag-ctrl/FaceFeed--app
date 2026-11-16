package com.example.data

import com.example.model.*

object SampleData {
    val currentUser = User(
        id = "user_me",
        fullName = "Niyon Islam Sohag",
        username = "niyon_sohag",
        email = "smd224292@gmail.com",
        avatarUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&auto=format&fit=crop&q=80",
        coverUrl = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=1200&auto=format&fit=crop&q=80",
        bio = "অনবরত মা মাটি মানুষের জন্য🌱\nAlhamdulillah for everything🤲",
        isVerified = true,
        isOnline = true,
        lastSeenMinutesAgo = 0,
        friendCount = 5000,
        followerCount = 50,
        followingCount = 5,
        postCount = 2,
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

    val friendTanvir = User(
        id = "user_tanvir",
        fullName = "Tanvir Ahmed",
        username = "tanvir_ahmed",
        avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&auto=format&fit=crop&q=80",
        coverUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200&auto=format&fit=crop&q=80",
        isVerified = true,
        isOnline = true,
        friendCount = 4210,
        followerCount = 1200,
        followingCount = 340,
        postCount = 152,
        note = "Coding Face Feed 🔥",
        isFriend = true
    )

    val friendNusrat = User(
        id = "user_nusrat",
        fullName = "Nusrat Jahan",
        username = "nusrat_jahan",
        avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500&auto=format&fit=crop&q=80",
        coverUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=1200&auto=format&fit=crop&q=80",
        isVerified = false,
        isOnline = true,
        friendCount = 3890,
        followerCount = 890,
        followingCount = 210,
        postCount = 98,
        note = "নতুন ফটোশুট এর ছবিগুলো পাঠিয়েছি ✨",
        isFriend = true
    )

    val friendHasan = User(
        id = "user_hasan",
        fullName = "হাচান মিয়া",
        username = "hasan_mia",
        avatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=500&auto=format&fit=crop&q=80",
        coverUrl = "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=1200&auto=format&fit=crop&q=80",
        isVerified = false,
        isOnline = true,
        friendCount = 1890,
        followerCount = 450,
        note = "সবুজ প্রকৃতি 🌿",
        isFriend = true
    )

    val friendSamrat = User(
        id = "user_samrat",
        fullName = "MD Samrat",
        username = "md_samrat",
        avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500&auto=format&fit=crop&q=80",
        coverUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=1200&auto=format&fit=crop&q=80",
        isVerified = false,
        isOnline = true,
        friendCount = 5000,
        followerCount = 2300,
        note = "চায়ের আড্ডায় ☕",
        isFriend = true
    )

    val friendArifur = User(
        id = "user_arifur",
        fullName = "Arifur Rahman",
        username = "arifur_rahman",
        avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=500&auto=format&fit=crop&q=80",
        coverUrl = "https://images.unsplash.com/photo-1472214103451-9374bd1c798e?w=1200&auto=format&fit=crop&q=80",
        isVerified = false,
        isOnline = true,
        friendCount = 4999,
        followerCount = 760,
        note = "লুডু খেলবে কে? 🎲",
        isFriend = true
    )

    val friendSadia = User(
        id = "user_sadia",
        fullName = "সাদিয়া রহমান",
        username = "sadia_rahman",
        avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500&auto=format&fit=crop&q=80",
        coverUrl = "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=1200&auto=format&fit=crop&q=80",
        isVerified = false,
        isOnline = true,
        lastSeenMinutesAgo = 0,
        friendCount = 3100,
        followerCount = 540,
        note = "শুভ দিন সবাইকে 🌸",
        isFriend = true
    )

    val friendJannat = User(
        id = "user_jannat",
        fullName = "জান্নাতুল ফেরদৌস",
        username = "jannat_ferdous",
        avatarUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=500&auto=format&fit=crop&q=80",
        coverUrl = "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=1200&auto=format&fit=crop&q=80",
        isVerified = true,
        isOnline = true,
        lastSeenMinutesAgo = 0,
        friendCount = 4200,
        followerCount = 1200,
        note = "নতুন ছবি আসছে 📸",
        isFriend = true
    )

    val friendManu = User(
        id = "user_manu",
        fullName = "মানু জাহান",
        username = "manu_jahan",
        avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=500&auto=format&fit=crop&q=80",
        coverUrl = "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=1200&auto=format&fit=crop&q=80",
        isVerified = false,
        isOnline = true,
        lastSeenMinutesAgo = 0,
        friendCount = 2800,
        followerCount = 890,
        isFriend = true
    )

    val friendRiya = User(
        id = "user_riya",
        fullName = "রিয়া মনি",
        username = "riya_moni",
        avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80",
        coverUrl = "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=1200&auto=format&fit=crop&q=80",
        isVerified = true,
        isOnline = true,
        lastSeenMinutesAgo = 0,
        friendCount = 3900,
        followerCount = 2100,
        isFriend = true
    )

    val friendShunya = User(
        id = "user_shunya",
        fullName = "শূন্য হৃদয়",
        username = "shunya_hridoy",
        avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500&auto=format&fit=crop&q=80",
        coverUrl = "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=1200&auto=format&fit=crop&q=80",
        isVerified = false,
        isOnline = true,
        lastSeenMinutesAgo = 0,
        friendCount = 1900,
        followerCount = 450,
        isFriend = true
    )

    val friendUmor = User(
        id = "user_umor",
        fullName = "UMOR FARUK",
        username = "umor_faruk",
        avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&auto=format&fit=crop&q=80",
        coverUrl = "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=1200&auto=format&fit=crop&q=80",
        isVerified = false,
        isOnline = true,
        lastSeenMinutesAgo = 0,
        friendCount = 3500,
        followerCount = 980,
        isFriend = true
    )

    val friendSahadat = User(
        id = "user_sahadat",
        fullName = "Sahadat Hossain",
        username = "sahadat_hossain",
        avatarUrl = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=500&auto=format&fit=crop&q=80",
        coverUrl = "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=1200&auto=format&fit=crop&q=80",
        isVerified = false,
        isOnline = false,
        lastSeenMinutesAgo = 45,
        friendCount = 5200,
        followerCount = 8900,
        isFriend = true
    )

    val friendSahid = User(
        id = "user_sahid",
        fullName = "Sahid Membar",
        username = "sahid_membar",
        avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&auto=format&fit=crop&q=80",
        coverUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200&auto=format&fit=crop&q=80",
        isVerified = false,
        isOnline = true,
        friendCount = 4850,
        followerCount = 1520,
        isFriend = true
    )

    // Users with incoming friend requests or suggestions (7 real requests matching screenshot badge)
    val pendingRequestUsers = listOf(
        User(
            id = "req_1",
            fullName = "Rakib Hasan",
            username = "rakib_hasan_01",
            avatarUrl = "https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=500&auto=format&fit=crop&q=80",
            friendCount = 412,
            hasReceivedFriendRequest = true
        ),
        User(
            id = "req_2",
            fullName = "Farhana Islam",
            username = "farhana_islam",
            avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=500&auto=format&fit=crop&q=80",
            friendCount = 1205,
            hasReceivedFriendRequest = true
        ),
        User(
            id = "req_3",
            fullName = "Mehedi Hasan Babu",
            username = "mehedi_babu",
            avatarUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=500&auto=format&fit=crop&q=80",
            friendCount = 2800,
            hasReceivedFriendRequest = true
        ),
        User(
            id = "req_4",
            fullName = "Sumaiya Akter",
            username = "sumaiya_akter_bd",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80",
            friendCount = 980,
            hasReceivedFriendRequest = true
        ),
        User(
            id = "req_5",
            fullName = "Tanvir Rahman",
            username = "tanvir_rahman",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&auto=format&fit=crop&q=80",
            friendCount = 1450,
            hasReceivedFriendRequest = true
        ),
        User(
            id = "req_6",
            fullName = "Jannatul Ferdous",
            username = "jannat_ferdous",
            avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500&auto=format&fit=crop&q=80",
            friendCount = 2100,
            hasReceivedFriendRequest = true
        ),
        User(
            id = "req_7",
            fullName = "Sakib Al Hasan",
            username = "sakib_fan_bd",
            avatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=500&auto=format&fit=crop&q=80",
            friendCount = 3400,
            hasReceivedFriendRequest = true
        )
    )

    val suggestedUsers = listOf(
        User(
            id = "sug_1",
            fullName = "Mahmudul Hasan Joy",
            username = "joy_cricket",
            avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500&auto=format&fit=crop&q=80",
            friendCount = 4995
        ),
        User(
            id = "sug_2",
            fullName = "Tariqul Islam (Celebrity)",
            username = "tariqul_official",
            avatarUrl = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=500&auto=format&fit=crop&q=80",
            friendCount = 5000,
            followerCount = 45000,
            isVerified = true
        ),
        User(
            id = "sug_3",
            fullName = "Fatema Tuz Zohra",
            username = "fatema_zohra",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80",
            friendCount = 2300
        )
    )

    // Friends list for Profile Screen matching Screenshot_20260920_143535.jpg
    val profileFriends = listOf(
        ProfileFriend(
            id = "pf_rahim",
            name = "Rahim Aly",
            username = "rahim_aly",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&auto=format&fit=crop&q=80",
            mutualFriendsCount = "65 mutual friends",
            isOnline = true
        ),
        ProfileFriend(
            id = "pf_joy",
            name = "Joy Ahmed",
            username = "joy_ahmed",
            avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500&auto=format&fit=crop&q=80",
            mutualFriendsCount = "135 mutual friends",
            isOnline = true
        ),
        ProfileFriend(
            id = "pf_yousuf",
            name = "Yousuf Farazi",
            username = "yousuf_farazi",
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=500&auto=format&fit=crop&q=80",
            mutualFriendsCount = "9+ new",
            isOnline = false
        ),
        ProfileFriend(
            id = "pf_hasan_m",
            name = "হাসান মাহমুদ সাইফুল্লাহ",
            username = "hasan_mahmud_s",
            avatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=500&auto=format&fit=crop&q=80",
            mutualFriendsCount = "161 mutual friends",
            isOnline = true
        ),
        ProfileFriend(
            id = "pf_mohammad",
            name = "Mohammad Ali",
            username = "mohammad_ali",
            avatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=500&auto=format&fit=crop&q=80",
            mutualFriendsCount = "42 mutual friends",
            isOnline = true
        ),
        ProfileFriend(
            id = "pf_lamia",
            name = "Lamia Islam",
            username = "lamia_islam",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500&auto=format&fit=crop&q=80",
            mutualFriendsCount = "88 mutual friends",
            isOnline = true
        ),
        ProfileFriend(
            id = "pf_tahmid",
            name = "Tahmid Hasan",
            username = "tahmid_hasan",
            avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=500&auto=format&fit=crop&q=80",
            mutualFriendsCount = "24 mutual friends",
            isOnline = false
        ),
        ProfileFriend(
            id = "pf_sahid",
            name = "Sahid Membar",
            username = "sahid_membar",
            avatarUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=500&auto=format&fit=crop&q=80",
            mutualFriendsCount = "12 mutual friends",
            isOnline = true
        ),
        ProfileFriend(
            id = "pf_rikta",
            name = "Rikta Akther",
            username = "rikta_akther",
            avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500&auto=format&fit=crop&q=80",
            mutualFriendsCount = "35 mutual friends",
            isOnline = true
        ),
        ProfileFriend(
            id = "pf_tanvir",
            name = "Tanvir Ahmed",
            username = "tanvir_ahmed",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&auto=format&fit=crop&q=80",
            mutualFriendsCount = "210 mutual friends",
            isOnline = true
        ),
        ProfileFriend(
            id = "pf_nusrat",
            name = "Nusrat Jahan",
            username = "nusrat_jahan",
            avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=500&auto=format&fit=crop&q=80",
            mutualFriendsCount = "180 mutual friends",
            isOnline = true
        ),
        ProfileFriend(
            id = "pf_samrat",
            name = "MD Samrat",
            username = "md_samrat",
            avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500&auto=format&fit=crop&q=80",
            mutualFriendsCount = "94 mutual friends",
            isOnline = true
        )
    )

    val initialStories = listOf(
        Story(
            id = "story_me",
            author = currentUser,
            mediaUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800&auto=format&fit=crop&q=80",
            timestamp = "Just now",
            caption = "Welcome to Face Feed! 🇧🇩"
        ),
        Story(
            id = "story_1",
            author = friendTanvir,
            mediaUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&auto=format&fit=crop&q=80",
            timestamp = "2h ago",
            caption = "Amazing weather today! 🌤️"
        ),
        Story(
            id = "story_2",
            author = friendNusrat,
            mediaUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=800&auto=format&fit=crop&q=80",
            timestamp = "4h ago",
            caption = "Photography tour in Cox's Bazar 🏖️"
        ),
        Story(
            id = "story_3",
            author = friendHasan,
            mediaUrl = "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=800&auto=format&fit=crop&q=80",
            timestamp = "5h ago",
            caption = "Green nature of Bangladesh 🌿"
        )
    )

    // 10 photo set for posts (5 top, 5 bottom)
    val tenSamplePhotos = listOf(
        "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1472214103451-9374bd1c798e?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1447752875215-b2761acb3c5d?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1473448912268-2022ce9509d8?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500&auto=format&fit=crop&q=80"
    )

    val initialPosts = listOf(
        Post(
            id = "post_sponsored_demo",
            author = User(
                id = "user_page_official",
                fullName = "Face Feed Official 🇧🇩",
                username = "facefeed_official",
                avatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&auto=format&fit=crop&q=80",
                isVerified = true
            ),
            timestamp = "Sponsored • 🚀 স্পন্সরড",
            title = "Face Feed বাংলাদেশ — নতুন সোশ্যাল নেটওয়ার্কে আপনাকে স্বাগতম!",
            text = "বাংলাদেশের যেকোনো সিম দিয়ে ০ এমবি ফ্রী ডাটায় ছবি ও পোস্ট শেয়ার করুন! নতুন নতুন ফিচার ও ভিডিও দেখুন। পোস্ট বুস্ট করুন ও হাজারো মানুষের কাছে আপনার কনটেন্ট ছড়িয়ে দিন।",
            hashtags = listOf("#FaceFeedBD", "#FreeData", "#Sponsored", "#BangladeshSocial"),
            images = listOf("https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=1200&auto=format&fit=crop&q=80"),
            reachViews = "350K+ 🚀",
            reactionCounts = mapOf(
                ReactionType.LOVE to 18400,
                ReactionType.LIKE to 12500,
                ReactionType.HUNDRED to 3200,
                ReactionType.CARE to 2900
            ),
            isBoosted = true,
            boostStatus = "ACTIVE",
            boostBudget = 1000,
            boostDays = 15,
            boostTargetReach = "১,২০,০০০+ মানুষ",
            boostTrxId = "BKH9281745",
            shareCount = 1450
        ),
        Post(
            id = "post_sahid",
            author = friendSahid,
            timestamp = "2h",
            location = "তালতলী উপজেলা, বরগুনা.",
            musicInfo = "Zulfiqer Russell • পর...",
            text = "॥জয় বাংলা॥\n॥জয় বঙ্গবন্ধু॥\n॥বাংলাদেশ আওয়ামী লীগ॥",
            hashtags = listOf("#JoyBangla", "#AwamiLeague", "#Barguna", "#Bangladesh"),
            images = listOf(
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800&auto=format&fit=crop&q=80"
            ),
            reachViews = "1.8M",
            reactionCounts = mapOf(
                ReactionType.LOVE to 14250,
                ReactionType.LIKE to 9400,
                ReactionType.CARE to 2100,
                ReactionType.WOW to 430
            ),
            userReaction = ReactionType.LIKE,
            comments = listOf(
                Comment(
                    id = "c_sahid_1",
                    postId = "post_sahid",
                    author = friendTanvir,
                    text = "জয় বাংলা 🇧🇩",
                    timestamp = "1h ago",
                    likeCount = 35
                )
            ),
            shareCount = 920
        ),
        Post(
            id = "post_pinned",
            author = currentUser,
            timestamp = "Nov 11, 2025 • 👥",
            text = "নিদ্রা চর সমুদ্র সৈকত 🏕️ 🌅 🏖️\nরাস উৎসবে নিদ্রা সৈকতে নিয়ে,, আমি একটি গান লিখছি এবং এই গানটি আমি নিয়ে গাইছি... সমুদ্রের উত্তাল হাওয়া আর হাজারো মানুষের ভালোবাসায় এক অবিস্মরণীয় অনুভূতি।",
            hashtags = listOf("#NidraBeach", "#SeaBeach", "#MusicFestival", "#NiyonIslamSohag", "#LiveSong", "#CoastalBangladesh"),
            images = listOf("https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=1000&auto=format&fit=crop&q=80"),
            reachViews = "1.2M",
            reactionCounts = mapOf(
                ReactionType.LOVE to 14200,
                ReactionType.LIKE to 9800,
                ReactionType.CARE to 2100,
                ReactionType.WOW to 1500
            ),
            userReaction = ReactionType.LOVE,
            reactors = listOf(
                ReactionUser(currentUser, ReactionType.LOVE, "Nov 11, 2025"),
                ReactionUser(friendTanvir, ReactionType.LIKE, "Nov 11, 2025"),
                ReactionUser(friendNusrat, ReactionType.CARE, "Nov 11, 2025")
            ),
            comments = listOf(
                Comment(
                    id = "c_pin_1",
                    postId = "post_pinned",
                    author = friendTanvir,
                    text = "ভাই আপনার এই গানটা অসাধারণ ছিল! কণ্ঠের সুর মনে দাগ কেটে গেছে ❤️",
                    timestamp = "Nov 11, 2025",
                    likeCount = 142
                )
            ),
            shareCount = 945
        ),
        Post(
            id = "post_1",
            author = currentUser,
            timestamp = "১০ মিনিট আগে • 🌐 Public",
            text = "আমার প্রিয় সোনার বাংলাদেশ 🇧🇩 সবাইকে FaceFeed এ আন্তরিক শুভেচ্ছা ও স্বাগতম! এখানে আমরা সবাই মিলে তৈরি করব এক নতুন স্বপ্নের সোশ্যাল প্ল্যাটফর্ম। নিচে আমাদের দেশ ও প্রকৃতির ১০টি অপরূপ দৃশ্য শেয়ার করলাম।",
            hashtags = listOf("#FaceFeed", "#Bangladesh", "#ViralPost", "#NiyonIslamSohag", "#Patriotism", "#Sylhet", "#Sundarbans", "#NatureLovers", "#BanglaSocial", "#TrendingNow"),
            images = tenSamplePhotos,
            reachViews = "2.4M",
            reactionCounts = mapOf(
                ReactionType.LOVE to 18450,
                ReactionType.LIKE to 12300,
                ReactionType.CARE to 5420,
                ReactionType.HUNDRED to 4100,
                ReactionType.WOW to 950
            ),
            userReaction = ReactionType.LOVE,
            reactors = listOf(
                ReactionUser(currentUser, ReactionType.LOVE, "১০ মিনিট আগে"),
                ReactionUser(friendTanvir, ReactionType.LIKE, "১৫ মিনিট আগে"),
                ReactionUser(friendNusrat, ReactionType.CARE, "২০ মিনিট আগে"),
                ReactionUser(friendSamrat, ReactionType.HUNDRED, "২৫ মিনিট আগে"),
                ReactionUser(friendArifur, ReactionType.OK, "৩০ মিনিট আগে")
            ),
            comments = listOf(
                Comment(
                    id = "c_1",
                    postId = "post_1",
                    author = friendTanvir,
                    text = "অসাধারণ সোহাগ ভাই! Face Feed সত্যিই বিশ্বমানের অ্যাপ হয়েছে 🔥❤️",
                    timestamp = "৮ মিনিট আগে",
                    likeCount = 84,
                    replies = listOf(
                        CommentReply(
                            id = "cr_1",
                            author = currentUser,
                            text = "ধন্যবাদ তানভীর ভাই! সাথে থাকুন ❤️💯",
                            timestamp = "৫ মিনিট আগে",
                            likeCount = 32
                        )
                    )
                ),
                Comment(
                    id = "c_2",
                    postId = "post_1",
                    author = friendNusrat,
                    text = "১০টি ছবি এবং ১০টি রিঅ্যাকশন ফিচারটি অনেক সুন্দর লেগেছে! শুভকামনা রইল 🥰✨",
                    timestamp = "৪ মিনিট আগে",
                    likeCount = 47
                )
            ),
            shareCount = 1820
        ),
        Post(
            id = "post_user_2",
            author = currentUser,
            timestamp = "২ দিন আগে • 🌐 Public",
            text = "অনবরত মা মাটি মানুষের জন্য কাজ করে যেতে চাই। সবার দোয়া ও ভালোবাসা কাম্য। 🌿🤲\n\nAlhamdulillah for everything!",
            hashtags = listOf("#NiyonIslamSohag", "#FaceFeed", "#Motherland", "#Bangladesh"),
            images = listOf("https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&auto=format&fit=crop&q=80"),
            reachViews = "450K",
            reactionCounts = mapOf(
                ReactionType.LOVE to 4520,
                ReactionType.LIKE to 3120,
                ReactionType.CARE to 980,
                ReactionType.HUNDRED to 850
            ),
            userReaction = ReactionType.LOVE,
            shareCount = 315
        ),
        Post(
            id = "post_2",
            author = friendTanvir,
            timestamp = "১ ঘণ্টা আগে • 🌐 Public",
            text = "আজকের রিলস ভিডিও: কক্সবাজারের সূর্যাস্তের অসাধারণ মুহূর্ত! Face Feed এ ভিডিও প্লেয়ার এবং অডিও কোয়ালিটি সুপার্ব 🌅",
            hashtags = listOf("#CoxsBazar", "#ShortVideo", "#Reels", "#SunsetVibes", "#TravelBD", "#Explore", "#FaceFeedReels", "#OceanView", "#Nature", "#Relax"),
            isVideo = true,
            isReel = true,
            videoTitle = "Cox's Bazar Sunset Drone Reel",
            videoDuration = "0:45",
            images = listOf("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&auto=format&fit=crop&q=80"),
            reachViews = "890K",
            reactionCounts = mapOf(
                ReactionType.LIKE to 6500,
                ReactionType.WOW to 2100,
                ReactionType.HUNDRED to 1450,
                ReactionType.LOVE to 3400
            ),
            shareCount = 612
        ),
        Post(
            id = "post_3",
            author = friendNusrat,
            timestamp = "৩ ঘণ্টা আগে • 👥 Friends",
            text = "চায়ের দেশ সিলেট ভ্রমণ 🍃 চা বাগানের অপরূপ সবুজ দৃশ্য দেখে চোখ জুড়িয়ে গেল। আপনারা সবাই কেমন আছেন?",
            hashtags = listOf("#Sylhet", "#TeaGarden", "#GreenBD", "#TravelStory", "#BangladeshNature", "#WeekendTrip", "#GreenVibes", "#Peace", "#Beauty", "#FaceFeedCommunity"),
            images = listOf(
                "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=500&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=500&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=500&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=500&auto=format&fit=crop&q=80"
            ),
            reachViews = "450K",
            reactionCounts = mapOf(
                ReactionType.CARE to 2100,
                ReactionType.LOVE to 4800,
                ReactionType.LIKE to 3100
            ),
            shareCount = 320
        ),
        Post(
            id = "post_4",
            author = friendSamrat,
            timestamp = "৫ ঘণ্টা আগে • 🌐 Public",
            text = "Face Feed এ ৫০০০ ফ্রেন্ড লিমিট পূরণ হওয়ার পর অটো ফলোয়ার চালু হয়ে গেল! অসাধারণ সিস্টেম 🚀 ধন্যবাদ সবাইকে!",
            hashtags = listOf("#5KFriends", "#AutoFollower", "#Milestone", "#FaceFeedApp", "#TechInnovation", "#SocialApp", "#BangladeshTech", "#Growth", "#Community", "#Verified"),
            reachViews = "1.8M",
            reactionCounts = mapOf(
                ReactionType.HUNDRED to 9200,
                ReactionType.LIKE to 8400,
                ReactionType.LOVE to 5100
            ),
            shareCount = 980
        )
    )

    val friendHasanMiaHowlader = User(
        id = "user_hasan_mia_howlader",
        fullName = "হাঢান মিয়া হওলাদার",
        username = "hasan_mia_howlader",
        avatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=500&auto=format&fit=crop&q=80",
        isOnline = true,
        isFriend = true
    )

    val friendSamim = User(
        id = "user_samim",
        fullName = "S A M I M ツ",
        username = "samim_bd",
        avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&auto=format&fit=crop&q=80",
        isOnline = true,
        isFriend = true
    )

    val friendRikta = User(
        id = "user_rikta",
        fullName = "Rikta Akther",
        username = "rikta_akther",
        avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500&auto=format&fit=crop&q=80",
        isOnline = false,
        isFriend = true
    )

    val friendJoyAhmed = User(
        id = "user_joy_ahmed",
        fullName = "Joy Ahmed",
        username = "joy_ahmed",
        avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500&auto=format&fit=crop&q=80",
        isOnline = true,
        isFriend = true
    )

    val userRahim = User(
        id = "user_rahim",
        fullName = "আব্দুর রহিম (Rahim)",
        username = "rahim_bangladesh",
        avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=500&auto=format&fit=crop&q=80",
        coverUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200&auto=format&fit=crop&q=80",
        isOnline = true,
        isFriend = true,
        accountCreationDate = "০১/০১/২০২৬ (1/1/2026)",
        friendCount = 3420,
        followerCount = 890
    )

    val friendMdMohiuddin = User(
        id = "user_md_mohiuddin",
        fullName = "MD Mohiuddin",
        username = "md_mohiuddin",
        avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&auto=format&fit=crop&q=80",
        coverUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200&auto=format&fit=crop&q=80",
        isOnline = true,
        isFriend = true,
        accountCreationDate = "০১/০১/২০২৬ (1/1/2026)",
        friendCount = 4250,
        followerCount = 1150
    )

    val userRahima = User(
        id = "user_rahima",
        fullName = "রহিমা খাতুন (Rahima)",
        username = "rahima_khatun",
        avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80",
        coverUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=1200&auto=format&fit=crop&q=80",
        isOnline = true,
        isFriend = true,
        accountCreationDate = "০৫/০২/২০২৬",
        friendCount = 4150,
        followerCount = 1240
    )

    val friendGroupUsers = User(
        id = "user_group_chat_fb",
        fullName = "Facebook user, মোহাম্মদ, রা...",
        username = "habibur_group",
        avatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=500&auto=format&fit=crop&q=80",
        isOnline = false,
        isFriend = true
    )

    val friendRahim = userRahim
    val friendKajol = friendSamim
    val friendShakib = friendSahadat
    val friendMim = friendRikta
    val friendSultana = userRahima

    val allAppUsersList: List<User> = listOf(
        currentUser,
        friendMdMohiuddin,
        friendTanvir,
        friendNusrat,
        friendHasan,
        friendSamrat,
        friendArifur,
        friendSadia,
        friendSahadat,
        friendSahid,
        friendHasanMiaHowlader,
        friendSamim,
        friendRikta,
        friendJoyAhmed,
        userRahim,
        userRahima
    )

    // MD Mohiuddin Chat Messages matching Screenshot 3
    private val msgMd1 = ChatMessage("md_1", "user_me", "Niyon Islam Sohag", "আসসালামু আলাইকুম,,", "8:10 PM", isFromMe = true)
    private val msgMd2 = ChatMessage("md_2", "user_me", "Niyon Islam Sohag", "কেমন আছো বন্ধু,,", "8:11 PM", isFromMe = true)
    private val msgMd3 = ChatMessage("md_3", "user_me", "Niyon Islam Sohag", "শুভ রাত্রি 💔", "8:12 PM", isFromMe = true)
    private val msgMd4 = ChatMessage("md_4", "user_md_mohiuddin", "MD Mohiuddin", "হুম ভালো তুমি", "8:14 PM", isFromMe = false, userReaction = "❤️")
    private val msgMd5 = ChatMessage("md_5", "user_md_mohiuddin", "MD Mohiuddin", "আছো কে", "8:15 PM", isFromMe = false)
    private val msgMd6 = ChatMessage("md_6", "user_me", "Niyon Islam Sohag", "আমি বাড়ি আছি বন্ধু,,", "8:16 PM", isFromMe = true, replyTo = msgMd5)
    private val msgMd7 = ChatMessage("md_7", "user_md_mohiuddin", "MD Mohiuddin", "ওহ", "8:17 PM", isFromMe = false)
    private val msgMd8 = ChatMessage("md_8", "user_me", "Niyon Islam Sohag", "হুম,,", "8:18 PM", isFromMe = true, replyTo = msgMd7)
    private val msgMd9 = ChatMessage("md_9", "user_md_mohiuddin", "MD Mohiuddin", "জাইতে চাইছিলাম কিন্তু সময় পাইনি তাই ঐ দিক যাইতে পারি নাই", "8:19 PM", isFromMe = false, userReaction = "😢")
    private val msgMd10 = ChatMessage("md_10", "user_me", "Niyon Islam Sohag", "তুমি ঢাকা কী চাকরি করো,,", "8:20 PM", isFromMe = true)
    private val msgMd11 = ChatMessage("md_11", "user_me", "Niyon Islam Sohag", "ঠিক আছে পরে দেখা হবে বন্ধু", "8:20 PM", isFromMe = true, replyTo = msgMd9)

    val initialConversations = listOf(
        Conversation(
            id = "chat_md_mohiuddin",
            friend = friendMdMohiuddin,
            lastMessage = "You: তুমি ঢাকা কী চাকরি করো,,",
            lastTime = "6:26 PM",
            unreadCount = 0,
            lastMessageTimestamp = System.currentTimeMillis() - 60_000L,
            messages = listOf(
                msgMd1, msgMd2, msgMd3, msgMd4, msgMd5,
                msgMd6, msgMd7, msgMd8, msgMd9, msgMd10, msgMd11
            )
        ),
        Conversation(
            id = "chat_rahim_rahima",
            friend = userRahim,
            lastMessage = "রহিমা: ঠিক আছে রহিম ভাই, আমি ফটো আর ভিডিও ফাইল পাঠিয়ে দিচ্ছি এখনই।",
            lastTime = "এখনই (Live)",
            unreadCount = 2,
            lastMessageTimestamp = System.currentTimeMillis() - 120_000L,
            messages = listOf(
                ChatMessage("rr_1", "user_rahim", "আব্দুর রহিম", "আসসালামু আলাইকুম রহিমা, কেমন আছো? কালকের প্রোগ্রামের ছবিগুলো কি রেডি?", "10:14 AM"),
                ChatMessage("rr_2", "user_rahima", "রহিমা খাতুন", "ওয়ালাইকুম আসসালাম রহিম ভাই। হ্যাঁ, সব ছবি সিলেক্ট করেছি। FaceFeed এ আপলোড দিব।", "10:15 AM"),
                ChatMessage("rr_3", "user_rahim", "আব্দুর রহিম", "অসাধারণ! প্রোগ্রামের প্রধান ফটোটা আমাকে ইনবক্সে পাঠাও তো একটু দেখি।", "10:16 AM"),
                ChatMessage(
                    id = "rr_4",
                    senderId = "user_rahima",
                    senderName = "রহিমা খাতুন",
                    text = "📷 এই যে অনুষ্ঠানের প্রধান ব্যানার ও স্টেজ ফটো।",
                    timestamp = "10:18 AM",
                    mediaUrl = "https://images.unsplash.com/photo-1511578314322-379afb476865?w=800",
                    isMediaVideo = false
                ),
                ChatMessage("rr_5", "user_rahim", "আব্দুর রহিম", "মাশাল্লাহ! ফটো খুব চমৎকার এসেছে। আর কোনো ভিডিও ক্লিপ আছে কি?", "10:20 AM"),
                ChatMessage(
                    id = "rr_6",
                    senderId = "user_rahima",
                    senderName = "রহিমা খাতুন",
                    text = "📹 হ্যাঁ, ১৫ সেকেন্ডের একটা সুন্দর হাইলাইটস রিলস ভিডিও বানিয়েছি। এই যে ভিডিও ক্লিপটা:",
                    timestamp = "10:22 AM",
                    mediaUrl = "https://images.unsplash.com/photo-1536240478700-b869070f9279?w=800",
                    isMediaVideo = true
                ),
                ChatMessage("rr_7", "user_rahim", "আব্দুর রহিম", "খুব সুন্দর হয়েছে! FaceFeed এ আজই পোস্ট করে দিও। সোহাগ ভাইয়ের Right Manager এ সবই সংরক্ষিত থাকে।", "10:25 AM"),
                ChatMessage("rr_8", "user_rahima", "রহিমা খাতুন", "ঠিক আছে রহিম ভাই, আমি ফটো আর ভিডিও ফাইল পাঠিয়ে দিচ্ছি এখনই।", "10:26 AM")
            )
        ),
        Conversation(
            id = "chat_hasan_mia",
            friend = friendHasanMiaHowlader,
            lastMessage = "You missed a call from হাঢান মিয়া.",
            lastTime = "12:12 PM",
            unreadCount = 1,
            lastMessageTimestamp = System.currentTimeMillis() - 3600_000L,
            messages = listOf(
                ChatMessage("hm_thumb", "user_hasan_mia_howlader", "হাঢান মিয়া হওলাদার", "👍", "12:10 PM"),
                ChatMessage("hm_pray", "user_hasan_mia_howlader", "হাঢান মিয়া হওলাদার", "🙏", "12:11 PM"),
                ChatMessage("hm_missed", "user_hasan_mia_howlader", "হাঢান মিয়া হওলাদার", "Missed audio call", "12:12 PM"),
                ChatMessage(
                    id = "hm_shared_post",
                    senderId = "user_hasan_mia_howlader",
                    senderName = "হাঢান মিয়া হওলাদার",
                    text = "ভুল স্বীকার করলো বানর",
                    timestamp = "10:43 AM",
                    mediaUrl = "https://images.unsplash.com/photo-1540573133985-87b6da6d54a9?w=800"
                )
            )
        ),
        Conversation(
            id = "chat_sahid_membar",
            friend = friendSahid,
            lastMessage = "You sent an attachment.",
            lastTime = "11:27 AM",
            unreadCount = 0,
            messages = listOf(
                ChatMessage("sm1", "user_me", "Niyon Islam Sohag", "You sent an attachment.", "11:27 AM", isFromMe = true)
            )
        ),
        Conversation(
            id = "chat_me_self",
            friend = currentUser,
            lastMessage = "You sent an attachment.",
            lastTime = "11:27 AM",
            unreadCount = 0,
            messages = listOf(
                ChatMessage("ms1", "user_me", "Niyon Islam Sohag", "You sent an attachment.", "11:27 AM", isFromMe = true)
            )
        ),
        Conversation(
            id = "chat_samim",
            friend = friendSamim,
            lastMessage = "📷 এই ছবিটা চেক করে দেখো তো ভাই",
            lastTime = "Sat",
            unreadCount = 0,
            messages = listOf(
                ChatMessage("sam1", "user_samim", "S A M I M ツ", "📷 এই ছবিটা চেক করে দেখো তো ভাই", "Sat", mediaUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500")
            )
        ),
        Conversation(
            id = "chat_rikta",
            friend = friendRikta,
            lastMessage = "কেমন আছেন আপনি?",
            lastTime = "Just now",
            unreadCount = 0,
            messages = listOf(
                ChatMessage("rk1", "user_rikta", "Rikta Akther", "আপনার মতো পোলা রে আমি পাত্তা ও দিই না!", "Sep 14"),
                ChatMessage("rk2", "user_rikta", "Rikta Akther", "📹 একটা ভিডিও ক্লিপ পাঠালাম", "Sep 14", mediaUrl = "https://images.unsplash.com/photo-1536240478700-b869070f9279?w=800", isMediaVideo = true),
                ChatMessage(
                    id = "rk3",
                    senderId = "user_me",
                    senderName = "Niyon Islam Sohag",
                    text = "কেমন আছেন আপনি?",
                    timestamp = "Just now",
                    isFromMe = true,
                    status = MessageDeliveryStatus.SENT
                )
            )
        ),
        Conversation(
            id = "chat_habibur",
            friend = friendGroupUsers,
            lastMessage = "Md Habibur: ❤️",
            lastTime = "Sep 14",
            unreadCount = 0,
            messages = listOf(
                ChatMessage("hb1", "user_group_chat_fb", "Md Habibur", "❤️", "Sep 14")
            )
        ),
        Conversation(
            id = "chat_joy",
            friend = friendJoyAhmed,
            lastMessage = "See you tomorrow bro 👍",
            lastTime = "Sep 10",
            unreadCount = 0,
            messages = listOf(
                ChatMessage("jy1", "user_joy_ahmed", "Joy Ahmed", "See you tomorrow bro 👍", "Sep 10")
            )
        ),
        Conversation(
            id = "chat_group",
            friend = friendTanvir,
            isGroup = true,
            groupName = "🚀 বন্ধুদের আড্ডা (Friends Circle)",
            groupMembers = listOf(currentUser, friendTanvir, friendNusrat, friendHasan, friendSamrat),
            lastMessage = "Sahadat: দারুণ আইডিয়া! Face Feed এ সবাই মিলে লাইভ করা যাক",
            lastTime = "Just now",
            unreadCount = 3,
            messages = listOf(
                ChatMessage("m1", "user_tanvir", "Tanvir Ahmed", "শুভ সন্ধ্যা বন্ধুরা!", "6:30 PM"),
                ChatMessage("m2", "user_nusrat", "Nusrat Jahan", "কেমন আছেন সবাই? গ্রুপ কল কবে হবে?", "6:32 PM"),
                ChatMessage("m3", "user_sahadat", "Sahadat Hossain", "দারুণ আইডিয়া! Face Feed এ সবাই মিলে লাইভ করা যাক", "6:35 PM")
            )
        ),
        Conversation(
            id = "chat_tanvir",
            friend = friendTanvir,
            lastMessage = "Face Feed এ আপনার সাথে কথা বলে খুব ভালো লাগলো ভাই!",
            lastTime = "২ মিনিট আগে",
            unreadCount = 1,
            messages = listOf(
                ChatMessage("tm1", "user_tanvir", "Tanvir Ahmed", "আসসালামু আলাইকুম সোহাগ ভাই!", "5:10 PM"),
                ChatMessage("tm2", "user_me", "Niyon Islam Sohag", "ওয়ালাইকুম আসসালাম! কেমন আছেন?", "5:12 PM", isFromMe = true),
                ChatMessage("tm3", "user_tanvir", "Tanvir Ahmed", "Face Feed এ আপনার সাথে কথা বলে খুব ভালো লাগলো ভাই!", "5:15 PM"),
                ChatMessage(
                    id = "tm4",
                    senderId = "user_tanvir",
                    senderName = "Tanvir Ahmed",
                    text = "🎤 অডিও ভয়েস (12s)",
                    timestamp = "5:16 PM",
                    isAudioVoice = true,
                    audioDurationSeconds = 12
                ),
                ChatMessage(
                    id = "tm5",
                    senderId = "user_me",
                    senderName = "Niyon Islam Sohag",
                    text = "🎤 অডিও ভয়েস (8s)",
                    timestamp = "5:18 PM",
                    isFromMe = true,
                    isAudioVoice = true,
                    audioDurationSeconds = 8
                )
            )
        ),
        Conversation(
            id = "chat_nusrat",
            friend = friendNusrat,
            lastMessage = "নতুন ফটোশুট এর ছবিগুলো পাঠিয়েছি, দেখবেন।",
            lastTime = "১৫ মিনিট আগে",
            unreadCount = 0,
            messages = listOf(
                ChatMessage("nm1", "user_nusrat", "Nusrat Jahan", "নতুন ফটোশুট এর ছবিগুলো পাঠিয়েছি, দেখবেন।", "4:40 PM")
            )
        ),
        Conversation(
            id = "chat_sadia",
            friend = friendSadia,
            lastMessage = "অডিও কলটা খুব ক্লিয়ার ছিল!",
            lastTime = "২ ঘণ্টা আগে",
            unreadCount = 0
        )
    )

    val notifActorFerdausFiroz = User(
        id = "user_notif_ferdaus_firoz",
        fullName = "মোঃফেরদাউস হাওলাদার, Md Firoz",
        username = "ferdaus_firoz",
        avatarUrl = "https://images.unsplash.com/photo-1544717305-2782549b5136?w=400"
    )
    val notifActorTax = User(
        id = "user_notif_tax",
        fullName = "FaceFeed Financial",
        username = "facefeed_tax",
        avatarUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=400"
    )
    val notifActorKamalMia = User(
        id = "user_notif_kamal_mia",
        fullName = "MD Kamal Mia",
        username = "kamal_mia",
        avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400"
    )
    val notifActorGaziRuhul = User(
        id = "user_notif_gazi_ruhul",
        fullName = "Gazi Ruhul",
        username = "gazi_ruhul",
        avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400"
    )
    val notifActorGaziMehede = User(
        id = "user_notif_gazi_mehede",
        fullName = "Gazi Mehede Hasan",
        username = "gazi_mehede",
        avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400"
    )
    val notifActorMahmuda = User(
        id = "user_notif_mahmuda",
        fullName = "Mahmuda M",
        username = "mahmuda_m",
        avatarUrl = "https://images.unsplash.com/photo-1518895949257-7621c3c786d7?w=400"
    )

    val initialNotifications = listOf(
        // NEW SECTION
        AppNotification(
            id = "notif_1",
            actor = notifActorFerdausFiroz,
            title = "মোঃফেরদাউস হাওলাদার, Md Firoz and 4 others reacted to your story.",
            timeAgo = "3h",
            type = NotificationType.STORY_REACTION,
            isNewSection = true,
            isRead = false,
            subBadgeType = "heart"
        ),
        AppNotification(
            id = "notif_2",
            actor = notifActorTax,
            title = "Complete your tax info now so you're ready to get paid.",
            timeAgo = "3h",
            type = NotificationType.TAX_INFO,
            isNewSection = true,
            isRead = false, // Highlighted blue unread in screenshot
            subBadgeType = "tax"
        ),
        AppNotification(
            id = "notif_3",
            actor = notifActorKamalMia,
            title = "MD Kamal Mia invited you to join the public group ⭐ Star sender.",
            timeAgo = "54m",
            type = NotificationType.GROUP_INVITE,
            isNewSection = true,
            isRead = true,
            hasActionButtons = true,
            subBadgeType = "group"
        ),

        // EARLIER SECTION
        AppNotification(
            id = "notif_4",
            actor = notifActorGaziRuhul,
            title = "Gazi Ruhul mentioned you in a comment: \"Niyon Islam Sohag ok thanks\".",
            timeAgo = "1d",
            type = NotificationType.COMMENT,
            isNewSection = false,
            isRead = true,
            subBadgeType = "comment"
        ),
        AppNotification(
            id = "notif_5",
            actor = notifActorGaziMehede,
            title = "Gazi Mehede Hasan accepted your friend request.",
            timeAgo = "1d",
            type = NotificationType.FRIEND_REQUEST,
            isNewSection = false,
            isRead = true,
            subBadgeType = "friend_accept"
        ),
        AppNotification(
            id = "notif_6",
            actor = notifActorMahmuda,
            title = "Mahmuda M likes your post in নিদ্রার চর সমুদ্র সৈকত & ডিসি পয়েন্ট তালতলী বরগুনা: \"আসসালামু আলাইকুম🤝 প্রিয় ভাই ভোন ও...\"",
            timeAgo = "1d",
            type = NotificationType.LIKE,
            isNewSection = false,
            isRead = true,
            subBadgeType = "like"
        ),
        AppNotification(
            id = "notif_7",
            actor = friendTanvir,
            title = "Today is Tanvir Ahmed's birthday. Wish him a happy birthday! 🎂🎉",
            timeAgo = "2h",
            type = NotificationType.BIRTHDAY,
            isNewSection = true,
            isRead = false,
            hasActionButtons = true,
            subBadgeType = "birthday"
        ),
        AppNotification(
            id = "notif_8",
            actor = friendNusrat,
            title = "Nusrat Jahan sent you a friend request.",
            timeAgo = "5h",
            type = NotificationType.FRIEND_REQUEST,
            isNewSection = true,
            isRead = false,
            hasActionButtons = true,
            subBadgeType = "friend_request"
        ),
        AppNotification(
            id = "notif_9",
            actor = friendSamrat,
            title = "MD Samrat shared your post to his timeline: 'অসাধারণ পোস্ট ভাই!'",
            timeAgo = "1h",
            type = NotificationType.SHARE,
            isNewSection = true,
            isRead = false,
            subBadgeType = "share"
        ),
        AppNotification(
            id = "notif_10",
            actor = notifActorMahmuda,
            title = "Daily Bangladesh 🇧🇩 invited you to follow their official page.",
            timeAgo = "4h",
            type = NotificationType.PAGE_LIKE,
            isNewSection = true,
            isRead = false,
            hasActionButtons = true,
            subBadgeType = "page"
        )
    )

    val initialGroups = listOf(
        SocialGroup(
            id = "grp_1",
            name = "🚀 বন্ধুদের আড্ডা (Friends Circle)",
            description = "সকল বন্ধুদের মিলনমেলা ও প্রতিদিনের গল্প-আড্ডা",
            coverUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=800&auto=format&fit=crop&q=80",
            memberCount = 12400,
            isPrivate = false,
            isJoined = true,
            creatorName = "Niyon Islam Sohag",
            isActive = true,
            createdAt = "০১/০১/২০২৬ (1/1/2026)",
            admins = listOf(
                currentUser,
                friendTanvir,
                friendHasanMiaHowlader,
                friendSamrat,
                friendSadia
            ),
            moderators = listOf(
                friendJannat,
                friendManu,
                friendRiya,
                friendShunya,
                friendUmor,
                friendMdMohiuddin,
                friendSahid,
                friendJoyAhmed,
                friendArifur,
                friendNusrat,
                friendRahim,
                friendKajol,
                friendShakib,
                friendMim,
                friendSultana
            )
        ),
        SocialGroup(
            id = "grp_2",
            name = "🇧🇩 Bangladesh Tech Developers & Creators",
            description = "প্রযুক্তি, কোডিং এবং অ্যাপ ডেভেলপমেন্ট বিষয়ক আলোচনা",
            coverUrl = "https://images.unsplash.com/photo-1515378791036-0648a3ef77b2?w=800&auto=format&fit=crop&q=80",
            memberCount = 45800,
            isPrivate = false,
            isJoined = true,
            creatorName = "Tanvir Ahmed",
            isActive = true,
            createdAt = "১০/০৫/২০২৬",
            admins = listOf(friendTanvir, currentUser, friendSamrat, friendArifur, friendSadia),
            moderators = listOf(friendJannat, friendManu, friendRiya, friendShunya, friendUmor)
        ),
        SocialGroup(
            id = "grp_3",
            name = "📸 Dhaka Street Photography Hub",
            description = "ঢাকাসহ সারা দেশের সেরা ফ্রেম ও ছবি প্রদর্শনী",
            coverUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=800&auto=format&fit=crop&q=80",
            memberCount = 8900,
            isPrivate = true,
            isJoined = false,
            creatorName = "Nusrat Jahan",
            isActive = true,
            createdAt = "১৫/০৬/২০২৬",
            admins = listOf(friendNusrat, currentUser, friendSadia),
            moderators = listOf(friendJannat, friendRiya, friendMim)
        ),
        SocialGroup(
            id = "grp_4",
            name = "🌾 আব্দুর রহিম কৃষি ও গ্রামীণ বাজার",
            description = "গ্রামীণ কৃষিপণ্য বেচাকেনা ও সহযোগিতা গ্রুপ",
            coverUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=800&auto=format&fit=crop&q=80",
            memberCount = 5200,
            isPrivate = false,
            isJoined = false,
            creatorName = "আব্দুর রহিম (Rahim)",
            isActive = true,
            createdAt = "০১/০১/২০২৬ (1/1/2026)",
            admins = listOf(friendRahim, currentUser),
            moderators = listOf(friendSahid, friendJoyAhmed, friendUmor)
        )
    )

    val initialPages = listOf(
        SocialPage(
            id = "page_1",
            name = "Face Feed Official 🇧🇩",
            category = "Social Media Platform",
            avatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&auto=format&fit=crop&q=80",
            coverUrl = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=1200&auto=format&fit=crop&q=80",
            followerCount = 1250000,
            isLiked = true,
            creatorName = "Niyon Islam Sohag",
            isActive = true,
            createdAt = "০১/০১/২০২৬ (1/1/2026)"
        ),
        SocialPage(
            id = "page_2",
            name = "Bangladesh Tourism & Nature",
            category = "Travel & Community",
            avatarUrl = "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=500&auto=format&fit=crop&q=80",
            coverUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200&auto=format&fit=crop&q=80",
            followerCount = 450000,
            isLiked = false,
            creatorName = "Tanvir Ahmed",
            isActive = true,
            createdAt = "১৫/০৩/২০২৬"
        ),
        SocialPage(
            id = "page_3",
            name = "রহিম ফ্যাশন ও শপিং কালেকশন",
            category = "E-commerce & Business",
            avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=500&auto=format&fit=crop&q=80",
            coverUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=1200&auto=format&fit=crop&q=80",
            followerCount = 38000,
            isLiked = true,
            creatorName = "আব্দুর রহিম (Rahim)",
            isActive = true,
            createdAt = "০১/০১/২০২৬ (1/1/2026)"
        )
    )

    val initialFollowers: List<FollowerUser> = listOf(
        FollowerUser("fl_1", "Rakib Hasan", "rakib_hasan", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200", 14, isFollowingBack = true),
        FollowerUser("fl_2", "Farhana Islam", "farhana_islam", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200", 8, isFollowingBack = false),
        FollowerUser("fl_3", "MD Samrat", "md_samrat_bd", "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=200", 22, isFollowingBack = true),
        FollowerUser("fl_4", "Arifur Rahman", "arif_rahman", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", 5, isFollowingBack = false),
        FollowerUser("fl_5", "Jannatul Ferdous", "jannat_ferdous", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200", 19, isFollowingBack = true),
        FollowerUser("fl_6", "Sakib Al Hasan", "sakib_h", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200", 31, isFollowingBack = false),
        FollowerUser("fl_7", "Mahmudul Haque", "mahmud_hq", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200", 12, isFollowingBack = true),
        FollowerUser("fl_8", "Sabina Yasmin", "sabina_y", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200", 7, isFollowingBack = false),
        FollowerUser("fl_9", "Rashedul Islam", "rashed_bd", "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=200", 15, isFollowingBack = true),
        FollowerUser("fl_10", "Mehedi Hasan", "mehedi_h", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200", 11, isFollowingBack = true),
        FollowerUser("fl_11", "Shamim Hossain", "shamim_hossain", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=200", 4, isFollowingBack = false),
        FollowerUser("fl_12", "Sonia Akter", "sonia_akter", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200", 17, isFollowingBack = true),
        FollowerUser("fl_13", "Moniruzzaman", "monir_zaman", "https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?w=200", 9, isFollowingBack = false),
        FollowerUser("fl_14", "Shahin Alam", "shahin_alam", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", 13, isFollowingBack = true),
        FollowerUser("fl_15", "Tania Sultana", "tania_sultana", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=200", 6, isFollowingBack = false),
        FollowerUser("fl_16", "Rony Chowdhury", "rony_chy", "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=200", 25, isFollowingBack = true),
        FollowerUser("fl_17", "Shirin Akter", "shirin_akter", "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?w=200", 18, isFollowingBack = false),
        FollowerUser("fl_18", "Asif Mahmud", "asif_mahmud", "https://images.unsplash.com/photo-1480429370139-e0132c086e2a?w=200", 10, isFollowingBack = true),
        FollowerUser("fl_19", "Nahid Islam", "nahid_islam", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200", 16, isFollowingBack = true),
        FollowerUser("fl_20", "Sumaiya Rahman", "sumaiya_r", "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?w=200", 8, isFollowingBack = false),
        FollowerUser("fl_21", "Imran Hossain", "imran_hossain", "https://images.unsplash.com/photo-1513956589380-bad6acb9b9d4?w=200", 12, isFollowingBack = true),
        FollowerUser("fl_22", "Kamrul Hasan", "kamrul_hasan", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200", 3, isFollowingBack = false),
        FollowerUser("fl_23", "Nasrin Sultana", "nasrin_s", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", 21, isFollowingBack = true),
        FollowerUser("fl_24", "Al Amin", "alamin_bd", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", 14, isFollowingBack = false),
        FollowerUser("fl_25", "Fatema Khatun", "fatema_k", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200", 7, isFollowingBack = true),
        FollowerUser("fl_26", "Rubel Mia", "rubel_mia", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200", 19, isFollowingBack = false),
        FollowerUser("fl_27", "Sadia Afrin", "sadia_afrin", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200", 11, isFollowingBack = true),
        FollowerUser("fl_28", "Biplob Kumar", "biplob_kumar", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=200", 5, isFollowingBack = false),
        FollowerUser("fl_29", "Mitu Akter", "mitu_akter", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=200", 16, isFollowingBack = true),
        FollowerUser("fl_30", "Jewel Rana", "jewel_rana", "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=200", 28, isFollowingBack = true),
        FollowerUser("fl_31", "Shampa Das", "shampa_das", "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?w=200", 9, isFollowingBack = false),
        FollowerUser("fl_32", "Faisal Ahmed", "faisal_ahmed", "https://images.unsplash.com/photo-1480429370139-e0132c086e2a?w=200", 15, isFollowingBack = true),
        FollowerUser("fl_33", "Poly Begum", "poly_begum", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200", 4, isFollowingBack = false),
        FollowerUser("fl_34", "Habibur Rahman", "habib_rahman", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200", 20, isFollowingBack = true),
        FollowerUser("fl_35", "Ruma Akter", "ruma_akter", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200", 13, isFollowingBack = false),
        FollowerUser("fl_36", "Saiful Islam", "saiful_islam", "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=200", 18, isFollowingBack = true),
        FollowerUser("fl_37", "Bilkis Banu", "bilkis_banu", "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?w=200", 6, isFollowingBack = false),
        FollowerUser("fl_38", "Liton Das", "liton_das_fan", "https://images.unsplash.com/photo-1513956589380-bad6acb9b9d4?w=200", 24, isFollowingBack = true),
        FollowerUser("fl_39", "Sharmin Akter", "sharmin_a", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200", 10, isFollowingBack = true),
        FollowerUser("fl_40", "Ripon Sheikh", "ripon_sheikh", "https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?w=200", 7, isFollowingBack = false),
        FollowerUser("fl_41", "Marufa Begum", "marufa_b", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200", 12, isFollowingBack = true),
        FollowerUser("fl_42", "Zahid Hasan", "zahid_hasan", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200", 29, isFollowingBack = true),
        FollowerUser("fl_43", "Taslima Nasrin", "taslima_n", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=200", 8, isFollowingBack = false),
        FollowerUser("fl_44", "Mizanur Rahman", "mizanur_r", "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=200", 17, isFollowingBack = true),
        FollowerUser("fl_45", "Salma Khatun", "salma_khatun", "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?w=200", 5, isFollowingBack = false),
        FollowerUser("fl_46", "Anwar Hossain", "anwar_h", "https://images.unsplash.com/photo-1480429370139-e0132c086e2a?w=200", 22, isFollowingBack = true),
        FollowerUser("fl_47", "Rina Akter", "rina_akter", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200", 11, isFollowingBack = false),
        FollowerUser("fl_48", "Golam Kibria", "golam_kibria", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200", 16, isFollowingBack = true),
        FollowerUser("fl_49", "Beauty Begum", "beauty_begum", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200", 9, isFollowingBack = false),
        FollowerUser("fl_50", "Rezaul Karim", "rezaul_karim", "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=200", 33, isFollowingBack = true)
    )

    val initialFollowing: List<FollowerUser> = listOf(
        FollowerUser("fg_1", "Tanvir Ahmed", "tanvir_ahmed", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", 42, isFollowingBack = true, isVerified = true),
        FollowerUser("fg_2", "Nusrat Jahan", "nusrat_jahan", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200", 38, isFollowingBack = true, isVerified = false),
        FollowerUser("fg_3", "Tariqul Islam", "tariqul_official", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200", 15, isFollowingBack = true, isVerified = true),
        FollowerUser("fg_4", "বাংলাদেশ আওয়ামী লীগ", "albd_official", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", 50, isFollowingBack = true, isVerified = true),
        FollowerUser("fg_5", "FaceFeed Official 🇧🇩", "facefeed_app", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200", 50, isFollowingBack = true, isVerified = true)
    )

    val galleryMediaItems: List<GalleryMediaItem> = listOf(
        // Camera Roll / ক্যামেরা থেকে তোলা ছবি
        GalleryMediaItem("gm_cam_1", "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800", "ক্যামেরা ছবি: সমুদ্র সৈকত ও সূর্যাস্ত", "Camera"),
        GalleryMediaItem("gm_cam_2", "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=800", "ক্যামেরা ছবি: সকালের রোদ ও গাছের পাতা", "Camera"),
        GalleryMediaItem("gm_cam_3", "https://images.unsplash.com/photo-1472214103451-9374bd1c798e?w=800", "ক্যামেরা ছবি: গ্রামের মেঠো পথ ও সবুজ বনানী", "Camera"),
        GalleryMediaItem("gm_cam_4", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800", "ক্যামেরা ছবি: কক্সবাজার সমুদ্র সৈকত ভিউ", "Camera"),
        GalleryMediaItem("gm_cam_5", "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=800", "ক্যামেরা ছবি: নদী ও পালতোলা নৌকা", "Camera"),
        GalleryMediaItem("gm_cam_6", "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=800", "ক্যামেরা ছবি: ভোরের কুয়াশা ও পাহাড়", "Camera"),
        GalleryMediaItem("gm_cam_7", "https://images.unsplash.com/photo-1447752875215-b2761acb3c5d?w=800", "ক্যামেরা ছবি: পাহাড়ি ঝর্ণাধারা ও অরণ্য", "Camera"),
        GalleryMediaItem("gm_cam_8", "https://images.unsplash.com/photo-1473448912268-2022ce9509d8?w=800", "ক্যামেরা ছবি: শরতের নীল আকাশ ও কাশফুল", "Camera"),
        GalleryMediaItem("gm_cam_9", "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=800", "ক্যামেরা ছবি: সুন্দরবনের ম্যানগ্রোভ দৃশ্য", "Camera"),
        GalleryMediaItem("gm_cam_10", "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=800", "ক্যামেরা ছবি: সাজেক ভ্যালির মেঘের রাজ্য", "Camera"),
        GalleryMediaItem("gm_cam_11", "https://images.unsplash.com/photo-1511497584788-87676104235f?w=800", "ক্যামেরা ছবি: বৃষ্টির দিনের কদম ফুল", "Camera"),
        GalleryMediaItem("gm_cam_12", "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800", "ক্যামেরা স্ন্যাপ: বন্ধুদের সাথে ফটোসেশন", "Camera"),

        // Phone Gallery / ফোন গ্যালারি অ্যালবাম
        GalleryMediaItem("gm_gal_1", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800", "গ্যালারি: স্টাইলিশ পোর্ট্রেট স্ন্যাপ", "Gallery"),
        GalleryMediaItem("gm_gal_2", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800", "গ্যালারি: ফ্রেন্ডস হ্যাঙ্গআউট", "Gallery"),
        GalleryMediaItem("gm_gal_3", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=800", "গ্যালারি: হাসিমুখ ও উৎসব", "Gallery"),
        GalleryMediaItem("gm_gal_4", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=800", "গ্যালারি: স্মার্ট প্রফেশনাল লুক", "Gallery"),
        GalleryMediaItem("gm_gal_5", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=800", "গ্যালারি: ট্রাভেল মোমেন্টস সিলেট", "Gallery"),
        GalleryMediaItem("gm_gal_6", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800", "গ্যালারি: মেমোরি অ্যালবাম ২০২৬", "Gallery"),
        GalleryMediaItem("gm_gal_7", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=800", "গ্যালারি: বন্ধুদের আড্ডা ও সেলফি", "Gallery"),
        GalleryMediaItem("gm_gal_8", "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=800", "গ্যালারি: ঢাকা বিশ্ববিদ্যালয়ের ক্যাম্পাস", "Gallery"),

        // Downloads & Saved Files / ডাউনলোড ফোল্ডার
        GalleryMediaItem("gm_dl_1", "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=1200", "ডাউনলোড: রঙিন এইচডি ওয়ালপেপার", "Downloads"),
        GalleryMediaItem("gm_dl_2", "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1200", "ডাউনলোড: তারাভরা রাতের আকাশ ওয়ালপেপার", "Downloads"),
        GalleryMediaItem("gm_dl_3", "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=800", "ডাউনলোড: মিউজিক অ্যালবাম আর্ট", "Downloads"),
        GalleryMediaItem("gm_dl_4", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=800", "ডাউনলোড: কনসার্ট লাইট ওয়ালপেপার", "Downloads"),
        GalleryMediaItem("gm_dl_5", "https://images.unsplash.com/photo-1518173946687-a4c8a383392e?w=800", "ডাউনলোড: পদ্মা সেতু পূর্ণাঙ্গ দৃশ্য", "Downloads"),
        GalleryMediaItem("gm_dl_6", "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=800", "ডাউনলোড: উৎসবের সাজসজ্জা ফটো", "Downloads"),

        // Short Videos & Reels / সট ভিডিও ও রিলস
        GalleryMediaItem("gm_rv_1", "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800", "রিলস: বৃষ্টিস্নাত বিকেলে চায়ের আড্ডা", "ShortVideo", isVideo = true, duration = "0:30"),
        GalleryMediaItem("gm_rv_2", "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=800", "রিলস: ঢাকার যানজট ও বিকেলের সুর", "ShortVideo", isVideo = true, duration = "0:45"),
        GalleryMediaItem("gm_rv_3", "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=800", "রিলস: পাহাড়ি পথে ট্রাভেল ব্লগ", "ShortVideo", isVideo = true, duration = "0:25"),
        GalleryMediaItem("gm_rv_4", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800", "রিলস: লাইভ মিউজিক কনসার্ট ড্রপ", "ShortVideo", isVideo = true, duration = "0:40"),
        GalleryMediaItem("gm_rv_5", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800", "রিলস: কক্সবাজার সমুদ্রের ঢেউ", "ShortVideo", isVideo = true, duration = "0:35"),

        // Long Videos / ফুল লোং ভিডিও
        GalleryMediaItem("gm_lv_1", "https://images.unsplash.com/photo-1536240478700-b869070f9279?w=800", "লোং ভিডিও: বাংলাদেশের নৈসর্গিক সৌন্দর্য ডকুমেন্টারি", "LongVideo", isVideo = true, duration = "14:20"),
        GalleryMediaItem("gm_lv_2", "https://images.unsplash.com/photo-1518173946687-a4c8a383392e?w=800", "লোং ভিডিও: স্বপ্নের পদ্মা সেতু ভ্রমণ কাহিনী", "LongVideo", isVideo = true, duration = "18:45"),
        GalleryMediaItem("gm_lv_3", "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=800", "লোং ভিডিও: নদীমাতৃক বাংলাদেশের জেলে জীবন ও সুর", "LongVideo", isVideo = true, duration = "11:15"),
        GalleryMediaItem("gm_lv_4", "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=800", "লোং ভিডিও: বান্দরবান ও সাজেক রোমাঞ্চকর জার্নি", "LongVideo", isVideo = true, duration = "22:10"),

        // Profiles & Avatars
        GalleryMediaItem("gm_pr_1", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800", "স্টাইলিশ পোর্ট্রেট", "Profile"),
        GalleryMediaItem("gm_pr_2", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800", "স্মার্ট প্রফেশনাল", "Profile"),
        GalleryMediaItem("gm_pr_3", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=800", "হাস্যোজ্জ্বল লুক", "Profile"),
        GalleryMediaItem("gm_pr_4", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=800", "ক্যাজুয়াল লুক", "Profile"),

        // Covers
        GalleryMediaItem("gm_cv_1", "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=1200", "রঙিন গ্রেডিয়েন্ট কভার", "Cover"),
        GalleryMediaItem("gm_cv_2", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1200", "সমুদ্র সৈকত কভার", "Cover"),
        GalleryMediaItem("gm_cv_3", "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1200", "পাহাড়ি তারাভরা রাত কভার", "Cover")
    )

    // ৫টি ও ততোধিক সামাজিক বিজ্ঞাপন (Social Welfare Advertisements)
    val sampleSocialAds: List<com.example.model.SocialAd> = listOf(
        com.example.model.SocialAd(
            id = "ad_social_1",
            title = "স্বেচ্ছায় রক্ত দিন, একটি জীবন বাঁচান 🩸",
            organization = "বাংলাদেশ রেড ক্রিসেন্ট সোসাইটি ও ব্লাড ডোনার্স ফোরাম",
            slogan = "আপনার এক ব্যাগ রক্ত বাঁচাতে পারে একটি মুমূর্ষু রোগীর প্রাণ",
            description = "রক্তদানে কোনো ক্ষতি হয় না, বরং শরীর সুস্থ থাকে এবং নতুন রক্তকণিকা তৈরি হয়। আজই স্বেচ্ছায় রক্তদানে এগিয়ে আসুন।",
            category = "রক্তদান",
            bannerImageUrl = "https://images.unsplash.com/photo-1615461066841-6116e61058f4?w=800",
            ctaText = "রক্তদাতা হিসেবে যুক্ত হোন",
            durationSeconds = 5
        ),
        com.example.model.SocialAd(
            id = "ad_social_2",
            title = "একটি গাছ লাগান, পরিবেশ ও আগামী প্রজন্ম বাঁচান 🌳",
            organization = "সামাজিক বনায়ন ও সবুজ বাংলাদেশ মিশন",
            slogan = "গাছ আমাদের পরম বন্ধু — অক্সিজেন দেয়, ছায়া দেয়, জলবায়ু রক্ষা করে",
            description = "আসুন নিজ আঙিনায়, রাস্তার পাশে কিংবা ছাদে অন্তত একটি করে ফলদ, বনজ বা ভেষজ গাছের চারা রোপণ করি।",
            category = "বৃক্ষরোপণ",
            bannerImageUrl = "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=800",
            ctaText = "বৃক্ষরোপণ কর্মসূচিতে অংশ নিন",
            durationSeconds = 5
        ),
        com.example.model.SocialAd(
            id = "ad_social_3",
            title = "প্রতিটি শিশুর জন্য মানসম্মত প্রাথমিক শিক্ষা 📚",
            organization = "সুবিধাবঞ্চিত পথশিশু শিক্ষা তহবিল ও ইউনিসেফ",
            slogan = "একটি খাতা ও একটি কলম বদলে দিতে পারে একটি শিশুর অন্ধকার জীবন",
            description = "পথশিশুদের খাতা, কলম, বই ও শিক্ষার সুযোগ করে দিন। শিশুশ্রম রোধ করি, শিশুদের মুখে বইয়ের আলো ফোটাই।",
            category = "শিশু শিক্ষা",
            bannerImageUrl = "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=800",
            ctaText = "শিশুর শিক্ষার দায়িত্ব নিন",
            durationSeconds = 5
        ),
        com.example.model.SocialAd(
            id = "ad_social_4",
            title = "ক্ষুধামুক্ত বাংলাদেশ — এক বেলা আহার মানবিক উদ্যোগ 🍚",
            organization = "ফুড ফর অল সামাজিক ফাউন্ডেশন বাংলাদেশ",
            slogan = "আপনার উদ্বৃত্ত খাদ্য ফেলে না দিয়ে অসহায় ক্ষুধার্তদের মাঝে বিলিয়ে দিন",
            description = "আজও বহু মানুষ অনাহারে রাত কাটায়। আসুন মানবিক সহায়তার হাত বাড়িয়ে অভাবী মানুষের মুখে অন্ন তুলে দিই।",
            category = "ক্ষুধামুক্ত সমাজ",
            bannerImageUrl = "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=800",
            ctaText = "এক বেলা খাবার উপহার দিন",
            durationSeconds = 5
        ),
        com.example.model.SocialAd(
            id = "ad_social_5",
            title = "নিরাপদ সড়ক চাই — জীবনের চেয়ে সময়ের মূল্য বেশি নয় 🚦",
            organization = "সামাজিক সড়ক নিরাপত্তা ও পথচারী সুরক্ষা পরিষদ",
            slogan = "সাবধানে গাড়ি চালান, ট্রাফিক আইন মেনে চলুন, দুর্ঘটনা রোধ করুন",
            description = "হেলমেট পরিধান করুন, ওভারটেকিং পরিহার করুন, জেব্রা ক্রসিং ও ফুটওভার ব্রিজ ব্যবহার করুন। নিজে বাঁচুন, অন্যকে বাঁচান।",
            category = "সড়ক সচেতনতা",
            bannerImageUrl = "https://images.unsplash.com/photo-1508962914676-134849a727f0?w=800",
            ctaText = "নিরাপদ সড়ক অঙ্গীকার করুন",
            durationSeconds = 5
        ),
        com.example.model.SocialAd(
            id = "ad_social_6",
            title = "প্লাস্টিক বর্জন করি, আমাদের নদী ও সমুদ্র রক্ষা করি 🌊",
            organization = "পরিবেশ ও নদী রক্ষা জাতীয় আন্দোলন",
            slogan = "একবার ব্যবহার্য প্লাস্টিক নদী ও জীববৈচিত্র্যের জন্য চরম হুমকি",
            description = "নদী ও খাল-বিলে পলিথিন ও প্লাস্টিক ফেলা বন্ধ করুন। পাটের ব্যাগ ব্যবহার করুন এবং আমাদের প্রিয় নদীগুলোকে বাঁচান।",
            category = "পরিবেশ সংরক্ষণ",
            bannerImageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800",
            ctaText = "নদী রক্ষা অভিযানে যোগ দিন",
            durationSeconds = 5
        ),
        com.example.model.SocialAd(
            id = "ad_social_7",
            title = "যুব সমাজকে কারিগরি ও প্রযুক্তি শিক্ষায় স্বাবলম্বীকরণ 💻",
            organization = "ডিজিটাল বাংলাদেশ স্কিল ডেভেলপমেন্ট কাউন্সিল",
            slogan = "দক্ষতাই ভবিষ্যৎ — ঘরে বসে ফ্রিল্যান্সিং ও কর্মসংস্থানের সুযোগ",
            description = "যুব সমাজকে আইটি, গ্রাফিক ডিজাইন, কোডিং ও আধুনিক প্রযুক্তিতে দক্ষ করে গড়ে তুলে বেকারত্ব দূরীকরণে অংশ নিন।",
            category = "যুব উন্নয়ন",
            bannerImageUrl = "https://images.unsplash.com/photo-1531482615713-2afd69097998?w=800",
            ctaText = "বিনামূল্যে আইটি প্রশিক্ষণ দেখুন",
            durationSeconds = 5
        )
    )

    val sampleLiveComments: List<LiveCommentItem> = listOf(
        LiveCommentItem("lc_1", "Tanvir Ahmed", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", "লাইভে যুক্ত হলাম ভাই! অনেক দিন পর দেখলাম ❤️"),
        LiveCommentItem("lc_2", "Nusrat Jahan", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200", "সাউন্ড একদম ক্লিয়ার আসছে ভাইয়া 👏✨"),
        LiveCommentItem("lc_3", "Sahidul Islam", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", "বরগুনা থেকে দেখছি! জয় বাংলা 🔥🇧🇩"),
        LiveCommentItem("lc_4", "Tariqul Islam", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200", "লাইভ শেয়ার করে দিলাম ভাইয়া! এগিয়ে যান 🚀"),
        LiveCommentItem("lc_5", "Fatema Zohra", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200", "FaceFeed এর ফিচারগুলো অসাধারণ হয়েছে 😍")
    )

    // Complete Music & Audio Tracks Catalog for Posts
    val sampleAudioTracks: List<PostAudioTrack> = listOf(
        // গজল (Islamic Ghazal)
        PostAudioTrack("aud_1", "হাসবি রাব্বি জাল্লাল্লাহ (হৃদয় জুড়ানো কাওয়ালি)", "সামি ইউসুফ ও বাংলা শিল্পীগোষ্ঠী", "গজল", "৪:১২", coverArtUrl = "https://images.unsplash.com/photo-1564769625905-50e93615e769?w=300"),
        PostAudioTrack("aud_2", "ইয়া নবী সালাম আলাইকা (মধুর কণ্ঠে গজল)", "আন্তর্জাতিক ইসলামিক নাশিদ", "গজল", "৩:৪৫", coverArtUrl = "https://images.unsplash.com/photo-1584551246679-0daf3d275d0f?w=300"),
        PostAudioTrack("aud_3", "আল্লাহু আল্লাহু তুমি জাল্লে শানহু", "বাংলা সুফি সংগীত", "গজল", "৩:২০", coverArtUrl = "https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa?w=300"),
        PostAudioTrack("aud_4", "তুমি রহমান তুমি মেহেরবান", "ইসলামিক হামদ ও নাত", "গজল", "৪:৩০", coverArtUrl = "https://images.unsplash.com/photo-1542816417-0983c9c9ad53?w=300"),

        // বাঁশির সুর (Soulful Flute Melodies)
        PostAudioTrack("aud_5", "পল্লী সুর - দূর গাঁয়ের মায়াবী বাঁশির তান", "ওস্তাদ আলাউদ্দিন খাঁ স্মৃতি বাদক", "বাঁশির সুর", "২:৫০", coverArtUrl = "https://images.unsplash.com/photo-1511192336575-5a79af67a629?w=300"),
        PostAudioTrack("aud_6", "ভাটিয়ালি বাঁশির সুর - নদীর কূলে সন্ধ্যার গান", "বাংলার লোক সুরকার", "বাঁশির সুর", "৩:১৫", coverArtUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=300"),
        PostAudioTrack("aud_7", "শান্ত স্নিগ্ধ সকালের বাঁশির মধুর ধুন", "মেডিটেশন ও নেচার মিউজিক", "বাঁশির সুর", "৩:৪০", coverArtUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=300"),
        PostAudioTrack("aud_8", "মেঘলা দিনের বিষাদ বাঁশি ও বৃষ্টির শব্দ", "মন কেমন করা সুর", "বাঁশির সুর", "২:৩৫", coverArtUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=300"),

        // জেমস ও আইয়ুব বাচ্চু (James & LRB Ayub Bachchu Classics)
        PostAudioTrack("aud_9", "বাংলাদেশ - এই দূর পরবাসে তারা ভরা রাতে", "নগর বাউল জেমস (James)", "জেমস ও আইয়ুব বাচ্চু", "৫:১০", coverArtUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=300"),
        PostAudioTrack("aud_10", "সেই তুমি কেন এতো অচেনা হলে", "আইয়ুব বাচ্চু (LRB)", "জেমস ও আইয়ুব বাচ্চু", "৪:৫৫", coverArtUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300"),
        PostAudioTrack("aud_11", "কবিতা তুমি স্বপ্নচারিণী হয়ে খবর নিও না", "নগর বাউল জেমস (James)", "জেমস ও আইয়ুব বাচ্চু", "৪:১৮", coverArtUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=300"),
        PostAudioTrack("aud_12", "কষ্ট পেতে ভালোবাসি তাই তোমার কাছে ছুটে আসি", "আইয়ুব বাচ্চু (LRB)", "জেমস ও আইয়ুব বাচ্চু", "৪:৪০", coverArtUrl = "https://images.unsplash.com/photo-1485579149621-3123dd979885?w=300"),
        PostAudioTrack("aud_13", "পদ্মা পাতার জল - জেমস", "নগর বাউল জেমস", "জেমস ও আইয়ুব বাচ্চু", "৩:৫০", coverArtUrl = "https://images.unsplash.com/photo-1445985543469-253c5bc697b2?w=300"),
        PostAudioTrack("aud_14", "তারা ভরা রাতে তোমার কথা মনে পড়ে", "আইয়ুব বাচ্চু (LRB)", "জেমস ও আইয়ুব বাচ্চু", "৫:০২", coverArtUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=300"),

        // ডিজে ও বিট মিউজিক (DJ Remix)
        PostAudioTrack("aud_15", "ডিজে বাংলা বাউন্স ক্লাব রিমিক্স ২০২৬", "ডিজে রাহাত ও ফ্রেন্ডস", "ডিজে গান", "৩:৩০", coverArtUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=300"),
        PostAudioTrack("aud_16", "বাংলা ফোক ডিজে ধামাকা পার্টি মিক্স", "ডিজে সনিক বিডি", "ডিজে গান", "৩:১৫", coverArtUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=300"),
        PostAudioTrack("aud_17", "বাস বুস্টার ইলেকট্রনিক ড্যান্স ড্রপ বিট", "বিডি ইডিএম লাভার্স", "ডিজে গান", "২:৪৫", coverArtUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=300"),

        // হিন্দি গান (Hindi Hit Melodies)
        PostAudioTrack("aud_18", "তুম হি হো (Tum Hi Ho - Aashiqui 2)", "অরিজিৎ সিং (Arijit Singh)", "হিন্দি গান", "৪:২২", coverArtUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=300"),
        PostAudioTrack("aud_19", "কেসারিয়া তেরা ইশক হ্যায় পিয়া (Kesariya)", "অরিজিৎ সিং ও প্রীতম", "হিন্দি গান", "৪:২৮", coverArtUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=300"),
        PostAudioTrack("aud_20", "তেরে ওয়াস্তে ফলক সে মে চাঁদ লাউঙ্গা", "শচীন-জিগার ও বরুণ জৈন", "হিন্দি গান", "৩:০৯", coverArtUrl = "https://images.unsplash.com/photo-1520523839898-507121400c46?w=300"),

        // দেশাত্মবোধক গান (Patriotic Songs)
        PostAudioTrack("aud_21", "আমার সোনার বাংলা আমি তোমায় ভালোবাসি", "জাতীয় সংগীত বাংলাদেশ 🇧🇩", "দেশাত্মবোধক", "৩:০০", coverArtUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=300"),
        PostAudioTrack("aud_22", "ধনধান্য পুষ্পভরা আমাদের এই বসুন্ধরা", "দ্বিজেন্দ্রলাল রায় - দেশাত্মবোধক", "দেশাত্মবোধক", "৩:৩০", coverArtUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=300"),
        PostAudioTrack("aud_23", "এক সাগর রক্তের বিনিময়ে বাংলার স্বাধীনতা", "স্বাধীন বাংলা বেতার কেন্দ্র", "দেশাত্মবোধক", "৩:৪০", coverArtUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=300"),
        PostAudioTrack("aud_24", "তীর হারা এই ঢেউয়ের সাগর পাড়ি দেব রে", "আপেল মাহমুদ - বীর মুক্তিযোদ্ধার গান", "দেশাত্মবোধক", "৩:১৫", coverArtUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=300"),

        // লালনগীতি ও বাউল (Lalon Geeti)
        PostAudioTrack("aud_25", "মিলন হবে কত দিনে আমার মনের মানুষের সনে", "ফকির লালন শাহ্ স্মৃতি সুর", "লালনগীতি", "৫:২০", coverArtUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300"),
        PostAudioTrack("aud_26", "খাঁচার ভিতর অচিন পাখি কেমনে আসে যায়", "ফরিদা পারভীন - লালন সুর", "লালনগীতি", "৪:১৫", coverArtUrl = "https://images.unsplash.com/photo-1511192336575-5a79af67a629?w=300"),
        PostAudioTrack("aud_27", "জাত গেল জাত গেল বলে একি আজব কারখানা", "বাউল সম্রাট লালনগীতি", "লালনগীতি", "৪:৪০", coverArtUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=300"),
        PostAudioTrack("aud_28", "সব লোকে কয় লালন কি জাত সংসারে", "বাংলার বাউল আখড়া", "লালনগীতি", "৩:৫০", coverArtUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=300"),

        // রবীন্দ্র সংগীত ও বিশ্বকবি (Rabindra Sangeet)
        PostAudioTrack("aud_29", "পুরানো সেই দিনের কথা ভুলবি কি রে হায়", "বিশ্বকবি রবীন্দ্রনাথ ঠাকুর", "রবীন্দ্র সংগীত", "৩:১০", coverArtUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=300"),
        PostAudioTrack("aud_30", "তুমি রবে নীরবে হৃদয়ে মম", "রবীন্দ্র ক্লাসিক সুর", "রবীন্দ্র সংগীত", "৪:০৫", coverArtUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=300"),
        PostAudioTrack("aud_31", "যদি তোর ডাক শুনে কেউ না আসে তবে একলা চলো রে", "বিশ্বকবির অমর অনুপ্রেরণা", "রবীন্দ্র সংগীত", "৩:২৫", coverArtUrl = "https://images.unsplash.com/photo-1472214103451-9374bd1c798e?w=300"),
        PostAudioTrack("aud_32", "আমার পরান যাহা চায় তুমি তাই", "হৃদয়ছোঁয়া রবীন্দ্র সংগীত", "রবীন্দ্র সংগীত", "৩:৫০", coverArtUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300"),

        // দুঃখের ও আবেগঘন সুর (Sad & Emotional Melodies)
        PostAudioTrack("aud_33", "একাকী নিঃসঙ্গ রাতের দীর্ঘশ্বাস - দুঃখের বাঁশি ও সুর", "স্যাড মেলোডি ইন্সট্রুমেন্টাল", "দুঃখের গান", "৩:৪০", coverArtUrl = "https://images.unsplash.com/photo-1518173946687-a4c8a383392e?w=300"),
        PostAudioTrack("aud_34", "যে পাখি ঘর বোঝে না উড়াল দিল আকাশে", "বিরহের গভীর তান", "দুঃখের গান", "৪:১২", coverArtUrl = "https://images.unsplash.com/photo-1485579149621-3123dd979885?w=300"),
        PostAudioTrack("aud_35", "হারিয়ে যাওয়া পুরনো দিন আর না বলা কথা", "স্মৃতির পাতা থেকে", "দুঃখের গান", "৩:২৫", coverArtUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=300"),
        PostAudioTrack("aud_36", "অভিমানী চোখের জল মুছে দেওয়ার কেউ নেই", "স্যাড ক্লাসিক ট্র্যাক", "দুঃখের গান", "৩:৫০", coverArtUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=300"),

        // জন্মদিনের গান (Birthday Celebration Songs - অনেক জন্মদিনের গান)
        PostAudioTrack("aud_bday_1", "শুভ জন্মদিন প্রিয় বন্ধু (Happy Birthday To You 🎂)", "বাংলা সেলিব্রেশন শিল্পীগোষ্ঠী", "শুভ জন্মদিন", "৩:১৫", coverArtUrl = "https://images.unsplash.com/photo-1513151233558-d860c5398176?w=300"),
        PostAudioTrack("aud_bday_2", "আজ তোমার জন্মদিন - আলোর রোশনাই ছড়িয়ে যাক 🎉", "সুর সম্রাট ও বার্থডে অর্কেস্ট্রা", "শুভ জন্মদিন", "৩:৪০", coverArtUrl = "https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?w=300"),
        PostAudioTrack("aud_bday_3", "বার্থডে ডিজে পার্টি ধামাকা মিক্স ২০২৬ 🎈🥳", "ডিজে পার্টি লাভার্স বিডি", "শুভ জন্মদিন", "২:৫০", coverArtUrl = "https://images.unsplash.com/photo-1530103862676-de8c9debad1d?w=300"),
        PostAudioTrack("aud_bday_4", "ছোট্ট সোনামণির শুভ জন্মদিন (Sweet Birthday Kids 🍰)", "শিশু কিশোর কণ্ঠশিল্পীরা", "শুভ জন্মদিন", "২:৪৫", coverArtUrl = "https://images.unsplash.com/photo-1527529482837-4698179dc6ce?w=300"),
        PostAudioTrack("aud_bday_5", "Happy Birthday To You (Acoustic Guitar & Piano 🎁)", "গ্লোবাল অ্যাকোস্টিক মেলোডি", "শুভ জন্মদিন", "৩:২০", coverArtUrl = "https://images.unsplash.com/photo-1512909006721-3d6018887383?w=300"),
        PostAudioTrack("aud_bday_6", "শুভ শুভ শুভ জন্মদিন - শত বছর বেঁচে থাকো তুমি 🕯️", "হৃদয়ছোঁয়া জন্মদিনের আশীর্বাদ সুর", "শুভ জন্মদিন", "৩:৩০", coverArtUrl = "https://images.unsplash.com/photo-1520854221256-17451cc331bf?w=300"),
        PostAudioTrack("aud_bday_7", "বার্থডে কেক কাটার মধুর সুর ও বাঁশি 🍰🎶", "মেলোডি ইন্সট্রুমেন্টাল", "শুভ জন্মদিন", "৩:০৫", coverArtUrl = "https://images.unsplash.com/photo-1558636508-e0db3814bd1d?w=300"),
        PostAudioTrack("aud_bday_8", "বার্থডে রিমিক্স ২০২৬ - পার্টি নাইট সেলিব্রেশন 🎊", "ডিজে নাইটস ঢাকা", "শুভ জন্মদিন", "৩:১৫", coverArtUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=300"),

        // অডিও গান (Popular Bengali Audio Songs)
        PostAudioTrack("aud_song_1", "তুমি আমার ঘুম ভাঙ্গানিয়া গান", "বাংলা রোমান্টিক ক্লাসিক", "অডিও গান", "৪:১৫", coverArtUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300"),
        PostAudioTrack("aud_song_2", "মেঘ বলেছে যাব যাব - ছায়াসুন্দরী সুর", "আধুনিক বাংলা মেলোডি", "অডিও গান", "৩:৪৮", coverArtUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=300"),
        PostAudioTrack("aud_song_3", "ওরে নীল দরিয়া আমায় দে রে দে ছাড়িয়া", "অমর বাংলা লোকগীতি", "অডিও গান", "৪:০৫", coverArtUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=300"),
        PostAudioTrack("aud_song_4", "এই সুন্দর স্বর্ণালী সন্ধ্যায় একাকী বসে", "হারানো দিনের সোনালী গান", "অডিও গান", "৩:৩৫", coverArtUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=300"),

        // মিউজিক (Music / BGM / Relaxing Instrumental)
        PostAudioTrack("aud_music_1", "শান্ত স্নিগ্ধ পিয়ানো ও অ্যাকোস্টিক গিটার মিউজিক", "রিল্যাক্সিং সাউন্ডস বিডি", "মিউজিক", "৩:৪০", coverArtUrl = "https://images.unsplash.com/photo-1520523839898-507121400c46?w=300"),
        PostAudioTrack("aud_music_2", "বাংলার পল্লী মেঠো সুর ও শান্ত ব্যাকগ্রাউন্ড মিউজিক (BGM)", "ফোক অর্কেস্ট্রা ঢাকা", "মিউজিক", "৪:১২", coverArtUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=300"),
        PostAudioTrack("aud_music_3", "সিনেমাটিক রোমান্টিক ভায়োলিন ও সিন্থ মিউজিক", "সুর সাগর স্টুডিও", "মিউজিক", "৩:২৫", coverArtUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=300"),
        PostAudioTrack("aud_music_4", "ক্যাফে অ্যাকোস্টিক বিজিএম - মন জুড়ানো সুর", "বিডি অ্যাম্বিয়েন্স মিউজিক", "মিউজিক", "২:৫৫", coverArtUrl = "https://images.unsplash.com/photo-1445985543469-253c5bc697b2?w=300"),

        // জন্মদিন গান (Birthday Songs explicitly tagged)
        PostAudioTrack("aud_bday_9", "শুভ জন্মদিন গান - শত জনম বেঁচে রও তুমি 🎂", "বার্থডে সুর শিল্পীগোষ্ঠী", "জন্মদিন গান", "৩:২০", coverArtUrl = "https://images.unsplash.com/photo-1513151233558-d860c5398176?w=300"),
        PostAudioTrack("aud_bday_10", "আজ শুভ জন্মদিন তোমার - আলো ছড়াও ভুবনে 🎈", "বার্থডে মেলোডি কালেকশন", "জন্মদিন গান", "৩:১৫", coverArtUrl = "https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?w=300"),
        PostAudioTrack("aud_bday_11", "Happy Birthday Beat Mix ২০২৬ 🥳🎉", "বার্থডে পার্টি ডিজে", "জন্মদিন গান", "২:৫০", coverArtUrl = "https://images.unsplash.com/photo-1530103862676-de8c9debad1d?w=300")
    )

    // জন্মদিনের স্পেশাল ইমুজি কালেকশন (Birthday Emojis)
    val birthdayEmojis: List<String> = listOf(
        "🎂", "🎉", "🎈", "🥳", "🎁", "🕯️", "🍰", "🎊", 
        "🧁", "🎇", "💖", "🥂", "👑", "💐", "🌟", "🎶"
    )

    // 10 Admin Slots for Right Manager Fida (Controlled exclusively by Niyon Islam Sohag)
    val initialAdminSlots: List<AdminSlot> = listOf(
        AdminSlot(1, currentUser, "প্রধান প্রতিষ্ঠাতা ও সুপার অ্যাডমিন 👑", isSuperAdmin = true, assignedDate = "০১/০১/২০২৬ (1/1/2026)"),
        AdminSlot(2, friendTanvir, "সিনিয়র টেকনিক্যাল অ্যাডমিন", isSuperAdmin = false, assignedDate = "১৫/০২/২০২৬ (15/2/2026)"),
        AdminSlot(3, friendNusrat, "কন্টেন্ট ও সেফটি মডারেটর", isSuperAdmin = false, assignedDate = "১৮/০৩/২০২৬ (18/3/2026)"),
        AdminSlot(4, friendSamrat, "কমিউনিটি ম্যানেজার", isSuperAdmin = false, assignedDate = "০১/০৪/২০২৬ (1/4/2026)"),
        AdminSlot(5, null, "অ্যাডমিন পদ খালি (ফাঁকা)", isSuperAdmin = false),
        AdminSlot(6, null, "অ্যাডমিন পদ খালি (ফাঁকা)", isSuperAdmin = false),
        AdminSlot(7, null, "অ্যাডমিন পদ খালি (ফাঁকা)", isSuperAdmin = false),
        AdminSlot(8, null, "অ্যাডমিন পদ খালি (ফাঁকা)", isSuperAdmin = false),
        AdminSlot(9, null, "অ্যাডমিন পদ খালি (ফাঁকা)", isSuperAdmin = false),
        AdminSlot(10, null, "অ্যাডমিন পদ খালি (ফাঁকা)", isSuperAdmin = false)
    )

    // Feature 12: খারাপ মন্তব্যকারী ও সাইবার অপরাধী অডিট রেকর্ড (আইনগত প্রোফাইল সহ)
    val initialCyberOffenders: List<CyberOffenderReport> = listOf(
        CyberOffenderReport(
            id = "offender_01",
            userId = "user_bad_01",
            userName = "আবুল কাশেম (Abul Kashem)",
            userUsername = "kashem_hater",
            userAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
            reportedComment = "এই অ্যাপ ভালো না, সোয়েব/মালিক ফালতু ফাও কথা বলে!",
            targetPostTitle = "FaceFeed এর নতুন আপডেট প্রকাশিত হয়েছে!",
            timestamp = "০৪/১০/২০২৬ (4/10/2026) ১২:৪০ PM",
            nationalId = "১৯৮৮২৬৯২০১৯২৮৩২১",
            phoneNumber = "+৮৮০ ১৮১৯-৭৭৬৬৫৫",
            ipAddress = "103.112.54.90",
            deviceModel = "Xiaomi Redmi Note 10 Pro (Android 13)",
            location = "উত্তরা, ঢাকা - ১২৩০ (আইপি ও টাওয়ার লোকেশন ট্রেসড)",
            legalViolation = "সাইবার নিরাপত্তা আইন ২০২৩ ধারা ২৫ (মিথ্যা/মানহানিকর তথ্য প্রচার) ও ধারা ২৯ (মানহানি)",
            status = "UNDER_REVIEW"
        ),
        CyberOffenderReport(
            id = "offender_02",
            userId = "user_bad_02",
            userName = "রকিবুল ইসলাম (Rokibul Islam)",
            userUsername = "rokib_toxic",
            userAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
            reportedComment = "তোদের অ্যাপের মালিক ভন্ড ও ধান্দাবাজ!",
            targetPostTitle = "Right Manager Fida ফ্রী মেগা বুস্টের শুভ সূচনা",
            timestamp = "০৩/১০/২০২৬ (3/10/2026) ০৮:১৫ PM",
            nationalId = "১৯৯২১২৩৪৫৬৭৮৯০১২",
            phoneNumber = "+৮৮০ ১৭৫৫-১১২২৩৩",
            ipAddress = "103.205.71.18",
            deviceModel = "Realme 9 Pro+ (Android 14)",
            location = "আগ্রাবাদ, চট্টগ্রাম (সাইবার ক্রাইম সেল নিবন্ধিত)",
            legalViolation = "ডিজিটাল নিরাপত্তা ও সাইবার অবমাননা আইন ধারা ২৯ (কুরুচিপূর্ণ আক্রমণ ও মানহানি)",
            status = "LEGAL_NOTICE_SENT"
        ),
        CyberOffenderReport(
            id = "offender_03",
            userId = "user_bad_03",
            userName = "কামরুল হাসান (Kamrul Hasan)",
            userUsername = "kamrul_bet_promoter",
            userAvatar = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=150",
            reportedComment = "মালিকের পোস্টে বাজি ধরুন 1xbet লিংক থেকে আর নেশার আড্ডা দিন!",
            targetPostTitle = "সকল বন্ধুদের জন্য ভালোবাসা ও শুভেচ্ছা",
            timestamp = "০২/১০/২০২৬ (2/10/2026) ০৩:২২ PM",
            nationalId = "১৯৯৫৯৮৭৬৫৪৩২১০৯৮",
            phoneNumber = "+৮৮০ ১৬১১-৪৪৩৩২২",
            ipAddress = "118.179.201.44",
            deviceModel = "Samsung Galaxy M31",
            location = "রাজশাহী সদর, রাজশাহী",
            legalViolation = "জুয়া প্রতিরোধ আইন ১৮৬৭ ও সাইবার নিরাপত্তা আইন (নিষিদ্ধ জুয়া ও মাদক প্রচার)",
            status = "PERMANENT_BANNED"
        )
    )

    // Feature 13: অ্যাপ আপডেট নোটিশ ব্রডকাস্ট রেকর্ড
    val initialAppUpdateBroadcasts: List<AppUpdateBroadcast> = listOf(
        AppUpdateBroadcast(
            id = "update_2026_01",
            version = "v3.0.0 (Right Manager Fida ১৩ মাস্টার সংস্করণ)",
            title = "FaceFeed ও Right Manager Fida নতুন ১৩টি ফিচার আপডেট সম্পন্ন!",
            description = "সকল সম্মানিত ব্যবহারকারীদের জানানো যাচ্ছে যে, FaceFeed অ্যাপে ওনার ফ্রী মেগা বুস্ট (১M+ রিচ), সাইবার অপরাধী আইনি ট্র্যাকার, নেশা ও জুয়া বিরোধী কড়া সিকিউরিটি এবং অ্যাপ আপডেট ব্রডকাস্টার সিস্টেম সফলভাবে যুক্ত করা হয়েছে!",
            appLogoUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=300",
            ownerAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
            ownerName = "Niyon Islam Sohag (প্রধান প্রতিষ্ঠাতা)",
            releaseDate = "০১/০১/২০২৬ (1/1/2026)",
            isActive = true
        )
    )

    // Option 14: ফ্রী ফিদা ভেরিফাই বিতরণ ইতিহাস (১ জনকে কতো বার দিছি, কোন মাসে দিছি, তারিখ সহ)
    val initialFreeVerifiedGrantRecords: List<FreeVerifiedGrantRecord> = listOf(
        FreeVerifiedGrantRecord(
            id = "grant_rec_01",
            recipientUserId = "user_tanvir",
            recipientName = "Tanvir Ahmed",
            recipientUsername = "tanvir_ahmed",
            recipientAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500",
            grantCountTimes = 2, // ২ বার দেওয়া হয়েছে
            grantMonth = "অক্টোবর ২০২৬, মে ২০২৬",
            grantDateFormatted = "০৫ অক্টোবর ২০২৬, সকাল ১০:১৫ AM",
            historyInstances = listOf(
                GrantInstanceDetail(1, "১২ মে ২০২৬", "মে ২০২৬", "দুপুর ১২:৩০ PM", "৩,০০০ কোটা থেকে প্রথমবার ফ্রী Fida Verified প্রদান"),
                GrantInstanceDetail(2, "০৫ অক্টোবর ২০২৬", "অক্টোবর ২০২৬", "সকাল ১০:১৫ AM", "ওনার কর্তৃক দ্বিতীয়বার ফ্রী ভেরিফিকেশন রিনিউয়াল অনুমোদন")
            ),
            quotaDeducted = 2,
            remarks = "প্রধান প্রতিষ্ঠাতা ও ওনার নিয়ন ইসলাম সোহাগ কর্তৃক সরাসরি অনুমোদিত"
        ),
        FreeVerifiedGrantRecord(
            id = "grant_rec_02",
            recipientUserId = "user_sadia",
            recipientName = "Sadia Islam",
            recipientUsername = "sadia_islam",
            recipientAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500",
            grantCountTimes = 3, // ৩ বার দেওয়া হয়েছে
            grantMonth = "অক্টোবর ২০২৬, জুলাই ২০২৬, মার্চ ২০২৬",
            grantDateFormatted = "০৩ অক্টোবর ২০২৬, রাত ০৮:২০ PM",
            historyInstances = listOf(
                GrantInstanceDetail(1, "১০ মার্চ ২০২৬", "মার্চ ২০২৬", "সকাল ১১:০০ AM", "কোটা থেকে প্রথমবার ফ্রী Fida Verified প্রদান"),
                GrantInstanceDetail(2, "১৫ জুলাই ২০২৬", "জুলাই ২০২৬", "বিকাল ০৪:৪৫ PM", "দ্বিতীয়বার কোটা থেকে ফ্রী এক্সেস প্রদান"),
                GrantInstanceDetail(3, "০৩ অক্টোবর ২০২৬", "অক্টোবর ২০২৬", "রাত ০৮:২০ PM", "তৃতীয়বার ফ্রী কোটা রি-ভেরিফাই নিশ্চিতকরণ")
            ),
            quotaDeducted = 3,
            remarks = "ওনার বিশেষ কোটা থেকে ৩ বার অনুমোদিত"
        ),
        FreeVerifiedGrantRecord(
            id = "grant_rec_03",
            recipientUserId = "user_nusrat",
            recipientName = "Nusrat Jahan",
            recipientUsername = "nusrat_jahan",
            recipientAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500",
            grantCountTimes = 1, // ১ বার দেওয়া হয়েছে
            grantMonth = "সেপ্টেম্বর ২০২৬",
            grantDateFormatted = "১৫ সেপ্টেম্বর ২০২৬, দুপুর ০২:৩০ PM",
            historyInstances = listOf(
                GrantInstanceDetail(1, "১৫ সেপ্টেম্বর ২০২৬", "সেপ্টেম্বর ২০২৬", "দুপুর ০২:৩০ PM", "৩,০০০ কোটা থেকে প্রথমবার ফ্রী Fida Verified প্রদান")
            ),
            quotaDeducted = 1,
            remarks = "৩,০০০ কোটা থেকে প্রধান ওনার কর্তৃক সরাসরি অনুমোদিত"
        ),
        FreeVerifiedGrantRecord(
            id = "grant_rec_04",
            recipientUserId = "user_rafiqul",
            recipientName = "Rafiqul Islam",
            recipientUsername = "rafiqul_islam",
            recipientAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500",
            grantCountTimes = 1, // ১ বার দেওয়া হয়েছে
            grantMonth = "আগস্ট ২০২৬",
            grantDateFormatted = "২০ আগস্ট ২০২৬, সকাল ১১:৪৫ AM",
            historyInstances = listOf(
                GrantInstanceDetail(1, "২০ আগস্ট ২০২৬", "আগস্ট ২০২৬", "সকাল ১১:৪৫ AM", "কোটা থেকে প্রথমবার ফ্রী ভেরিফিকেশন প্রদান")
            ),
            quotaDeducted = 1,
            remarks = "কোটা থেকে ওনার অনুমোদিত"
        )
    )

    // Option 15: আমার কাছ থেকে ১ জনে কতো বার ভেরিফাই নিছে (তারিখ ও প্রোফাইল আইকন সহ অডিট লেজার)
    val initialUserVerificationAuditEntries: List<UserVerificationAuditEntry> = listOf(
        UserVerificationAuditEntry(
            id = "audit_entry_01",
            userId = "user_sadia",
            userName = "Sadia Islam",
            userUsername = "sadia_islam",
            userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500",
            totalTimesVerifiedTaken = 3, // মোট ৩ বার ভেরিফাই নিয়েছে
            currentBadgeStatus = "সক্রিয় ভেরিফাইড 🔵",
            historyInstances = listOf(
                VerificationInstanceRecord(1, "১০ মার্চ ২০২৬", "মার্চ ২০২৬", "সকাল ১১:০০ AM", "প্রধান প্রতিষ্ঠাতা নিয়ন ইসলাম সোহাগ", "ফ্রী কোটা অনুমোদন"),
                VerificationInstanceRecord(2, "১৫ জুলাই ২০২৬", "জুলাই ২০২৬", "বিকাল ০৪:৪৫ PM", "প্রধান প্রতিষ্ঠাতা নিয়ন ইসলাম সোহাগ", "কোটা রিনিউয়াল"),
                VerificationInstanceRecord(3, "০৩ অক্টোবর ২০২৬", "অক্টোবর ২০২৬", "রাত ০৮:২০ PM", "প্রধান প্রতিষ্ঠাতা নিয়ন ইসলাম সোহাগ", "ফ্রী কোটা রি-ভেরিফাই")
            )
        ),
        UserVerificationAuditEntry(
            id = "audit_entry_02",
            userId = "user_tanvir",
            userName = "Tanvir Ahmed",
            userUsername = "tanvir_ahmed",
            userAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500",
            totalTimesVerifiedTaken = 2, // মোট ২ বার ভেরিফাই নিয়েছে
            currentBadgeStatus = "সক্রিয় ভেরিফাইড 🔵",
            historyInstances = listOf(
                VerificationInstanceRecord(1, "১২ মে ২০২৬", "মে ২০২৬", "দুপুর ১২:৩০ PM", "প্রধান প্রতিষ্ঠাতা নিয়ন ইসলাম সোহাগ", "ফ্রী কোটা অনুমোদন"),
                VerificationInstanceRecord(2, "০৫ অক্টোবর ২০২৬", "অক্টোবর ২০২৬", "সকাল ১০:১৫ AM", "প্রধান প্রতিষ্ঠাতা নিয়ন ইসলাম সোহাগ", "ফ্রী কোটা রিনিউয়াল")
            )
        ),
        UserVerificationAuditEntry(
            id = "audit_entry_03",
            userId = "user_nusrat",
            userName = "Nusrat Jahan",
            userUsername = "nusrat_jahan",
            userAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500",
            totalTimesVerifiedTaken = 1, // মোট ১ বার ভেরিফাই নিয়েছে
            currentBadgeStatus = "সক্রিয় ভেরিফাইড 🔵",
            historyInstances = listOf(
                VerificationInstanceRecord(1, "১৫ সেপ্টেম্বর ২০২৬", "সেপ্টেম্বর ২০২৬", "দুপুর ০২:৩০ PM", "প্রধান প্রতিষ্ঠাতা নিয়ন ইসলাম সোহাগ", "ফ্রী কোটা অনুমোদন")
            )
        ),
        UserVerificationAuditEntry(
            id = "audit_entry_04",
            userId = "user_rafiqul",
            userName = "Rafiqul Islam",
            userUsername = "rafiqul_islam",
            userAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500",
            totalTimesVerifiedTaken = 1, // মোট ১ বার ভেরিফাই নিয়েছে
            currentBadgeStatus = "সক্রিয় ভেরিফাইড 🔵",
            historyInstances = listOf(
                VerificationInstanceRecord(1, "২০ আগস্ট ২০২৬", "আগস্ট ২০২৬", "সকাল ১১:৪৫ AM", "প্রধান প্রতিষ্ঠাতা নিয়ন ইসলাম সোহাগ", "ফ্রী কোটা অনুমোদন")
            )
        )
    )

    // User Story Highlights Collections (Friends এর নিচে হাইলাইটস - Screenshot 1 অনুযায়ী)
    val initialHighlightsMap: Map<String, List<StoryHighlight>> = mapOf(
        "user_me" to listOf(
            StoryHighlight(
                id = "hl_1",
                userId = "user_me",
                title = "আমি কিংবদন্তী...",
                coverUrl = "https://images.unsplash.com/photo-1544717305-2782549b5136?w=600",
                stories = (1..14).map { idx ->
                    Story("st_hl1_$idx", currentUser, "https://images.unsplash.com/photo-1544717305-2782549b5136?w=900", "$idx দিন আগে", false, "হৃদয়ের কিংবদন্তী মুহূর্ত ❤️")
                },
                createdAt = "০৫ অক্টোবর ২০২৬"
            ),
            StoryHighlight(
                id = "hl_2",
                userId = "user_me",
                title = "ঢাকা মেট্রোরেল...",
                coverUrl = "https://images.unsplash.com/photo-1509356843151-3e7d96241e11?w=600",
                stories = (1..9).map { idx ->
                    Story("st_hl2_$idx", currentUser, "https://images.unsplash.com/photo-1509356843151-3e7d96241e11?w=900", "$idx দিন আগে", false, "ঢাকা মেট্রোরেলের যাত্রা 🚆")
                },
                createdAt = "০১ অক্টোবর ২০২৬"
            ),
            StoryHighlight(
                id = "hl_3",
                userId = "user_me",
                title = "সমুদ্রের গল্প 🌻...",
                coverUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600",
                stories = (1..8).map { idx ->
                    Story("st_hl3_$idx", currentUser, "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=900", "$idx সপ্তাহ আগে", false, "সাগরের শান্ত বেলাভূমি 🌊")
                },
                createdAt = "২০ সেপ্টেম্বর ২০২৬"
            ),
            StoryHighlight(
                id = "hl_4",
                userId = "user_me",
                title = "ফিলিস্তিন 🌻🖤",
                coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600",
                stories = (1..16).map { idx ->
                    Story("st_hl4_$idx", currentUser, "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=900", "$idx দিন আগে", false, "শান্তি ও প্রার্থনার বার্তা 🕊️")
                },
                createdAt = "১৫ সেপ্টেম্বর ২০২৬"
            ),
            StoryHighlight(
                id = "hl_5",
                userId = "user_me",
                title = "চাষী'র 🌻🤎",
                coverUrl = "https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=600",
                stories = (1..16).map { idx ->
                    Story("st_hl5_$idx", currentUser, "https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=900", "$idx দিন আগে", false, "সোনার বাংলার রূপালী ধানক্ষেত 🌾")
                },
                createdAt = "১০ সেপ্টেম্বর ২০২৬"
            )
        ),
        "user_niyon" to listOf(
            StoryHighlight(
                id = "hl_niyon_1",
                userId = "user_niyon",
                title = "Official Moments 💎",
                coverUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500",
                stories = listOf(
                    Story("hl_n_1", currentUser, "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800", "১ দিন আগে", false, "CEO & Founder Diary 💼")
                ),
                createdAt = "০৩ অক্টোবর ২০২৬"
            ),
            StoryHighlight(
                id = "hl_niyon_2",
                userId = "user_niyon",
                title = "Travel ✈️",
                coverUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=500",
                stories = listOf(
                    Story("hl_n_2", currentUser, "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=800", "২ সপ্তাহ আগে", false, "Journey through Green hills 🏔️")
                ),
                createdAt = "২৫ সেপ্টেম্বর ২০২৬"
            )
        )
    )

    // Option 16: নির্দিষ্ট মেয়াদে আইডি সার্চপেন্ড (৩ দিন থেকে ৩ বছর) নমুনা রেকর্ড তালিকা
    val initialTimedSuspensions: List<UserSuspensionRecord> = listOf(
        UserSuspensionRecord(
            id = "susp_rec_1",
            userId = "user_tareq",
            userName = "Tareq Mahmud",
            userUsername = "tareq_bd",
            userAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500",
            durationText = "7 দিনের",
            durationIndex = 2,
            reasonNote = "গ্রুপে নিয়মবহির্ভূত আপত্তিকর মন্তব্য ও পোস্ট করার অপরাধে",
            suspendedDateFormatted = "০৮ অক্টোবর ২০২৬, সকাল ১০:৩০ AM",
            isSuspendedActive = true
        ),
        UserSuspensionRecord(
            id = "susp_rec_2",
            userId = "user_shakib",
            userName = "Shakib Al Hasan",
            userUsername = "shakib_75",
            userAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500",
            durationText = "14 দিনের",
            durationIndex = 3,
            reasonNote = "অন্য ব্যবহারকারীকে উস্কানিমূলক কথা ও মেসেজ পাঠানোর অপরাধে",
            suspendedDateFormatted = "০৫ অক্টোবর ২০২৬, দুপুর ০২:১৫ PM",
            isSuspendedActive = true
        ),
        UserSuspensionRecord(
            id = "susp_rec_3",
            userId = "user_rashed",
            userName = "Rashed Khan",
            userUsername = "rashed_khan_99",
            userAvatar = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=500",
            durationText = "1মাসের",
            durationIndex = 5,
            reasonNote = "স্প্যামিং ও ভুয়া কনটেন্ট বারবার ছড়ানোর অপরাধে",
            suspendedDateFormatted = "০১ অক্টোবর ২০২৬, বিকাল ০৫:০০ PM",
            isSuspendedActive = true
        )
    )

    // Support Team User Messages & Tickets (Screenshot 2 and Right Manager Fida Option)
    val sampleSupportTickets = listOf(
        SupportTicket(
            id = "ticket_1",
            userId = "user_joy",
            userName = "Joy Ahmed",
            userAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500",
            profileUrl = "https://facefeed.com/user/joy_ahmed",
            message = "আসসালামু আলাইকুম ভাই, আমার পেজে ভিডিও আপলোড করার পর ক্যাপশন এডিট করতে পারছি না। অনুগ্রহ করে সমাধান করে দিন। ৫টি স্ক্রিনশট সংযুক্ত করলাম।",
            screenshotUrls = listOf(
                "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500",
                "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=500",
                "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=500"
            ),
            timestamp = "আজ দুপুর ২:৩০ PM",
            status = "Open"
        ),
        SupportTicket(
            id = "ticket_2",
            userId = "user_tanvir",
            userName = "Tanvir Ahmed",
            userAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500",
            profileUrl = "https://facefeed.com/user/tanvir_ahmed",
            message = "ভাইয়া Fida Verified ব্লু ব্যাজ পাওয়ার জন্য সাবস্ক্রিপশন সম্পন্ন করেছি। বিকাশ পেমেন্ট আইডি দিয়েছি। দয়া করে একটু যাচাই করে কনফার্ম করবেন।",
            screenshotUrls = listOf(
                "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=500",
                "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?w=500"
            ),
            timestamp = "গতকাল রাত ৯:১৫ PM",
            status = "Resolved",
            adminReply = "আলহামদুলিল্লাহ ভাই, আপনার ভেরিফিকেশন যাচাই সফল হয়েছে এবং নীল ব্যাজ সক্রিয় করা হয়েছে!",
            adminReplyTime = "গতকাল রাত ৯:৪০ PM"
        )
    )

    // User Ad Analytics & Activity Logs (Right Manager Fida Option 24)
    val initialUserAdAnalytics = listOf(
        UserAdAnalytics(
            userId = "user_mohiuddin",
            userName = "MD Mohiuddin",
            userAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500",
            userHandle = "@mohiuddin_07",
            totalAdsSeen = 48,
            dailyAdsServed = 14,
            completedAdsWatched = 39,
            skippedAdsCount = 9,
            bannerAdsViewed = 26,
            interstitialAdsViewed = 22,
            lastAdSeenTime = "১০ মিনিট আগে",
            recentAdLogs = listOf(
                "ভিডিও প্লেব্যাকের ৩ সেকেন্ডে Interstitial অ্যাড সম্পূর্ণ দেখেছে (৫ সেকেন্ড)",
                "রিলস স্ক্রলে ছোট ব্যানার অ্যাড প্রদর্শন হয়েছে",
                "ভিডিও শেষে Interstitial অ্যাড স্কিপ করেছে"
            )
        ),
        UserAdAnalytics(
            userId = "user_joy",
            userName = "Joy Ahmed",
            userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=500",
            userHandle = "@joy_ahmed_bd",
            totalAdsSeen = 62,
            dailyAdsServed = 18,
            completedAdsWatched = 55,
            skippedAdsCount = 7,
            bannerAdsViewed = 34,
            interstitialAdsViewed = 28,
            lastAdSeenTime = "২৫ মিনিট আগে",
            recentAdLogs = listOf(
                "ভিডিওর নিচে বটম স্টিকি ব্যানার অ্যাড দেখেছে",
                "রিলস ভিডিওর ৫ সেকেন্ডের অ্যাড সম্পূর্ণ শেষ করেছে",
                "ভিডিও শেষে Interstitial অ্যাড সম্পূর্ণ দেখেছে"
            )
        ),
        UserAdAnalytics(
            userId = "user_tanvir",
            userName = "Tanvir Ahmed",
            userAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500",
            userHandle = "@tanvir_pro",
            totalAdsSeen = 35,
            dailyAdsServed = 11,
            completedAdsWatched = 24,
            skippedAdsCount = 11,
            bannerAdsViewed = 20,
            interstitialAdsViewed = 15,
            lastAdSeenTime = "১ ঘণ্টা আগে",
            recentAdLogs = listOf(
                "Interstitial অ্যাড ৫ সেকেন্ড পর স্কিপ বোতাম চেপে কেটে দিয়েছে",
                "মার্কেটপ্লেস ও ভিডিও পেজে ব্যানার অ্যাড দেখেছে"
            )
        ),
        UserAdAnalytics(
            userId = "user_samrat",
            userName = "Samrat Khan",
            userAvatar = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=500",
            userHandle = "@samrat_king",
            totalAdsSeen = 85,
            dailyAdsServed = 25,
            completedAdsWatched = 76,
            skippedAdsCount = 9,
            bannerAdsViewed = 45,
            interstitialAdsViewed = 40,
            lastAdSeenTime = "২ ঘণ্টা আগে",
            recentAdLogs = listOf(
                "৩টি ভিডিওর Interstitial অ্যাড একটানা সম্পূর্ণ দেখেছে",
                "লাইভ আড্ডা ও রিলসে ব্যানার অ্যাড দেখেছে"
            )
        ),
        UserAdAnalytics(
            userId = "user_picchi",
            userName = "পিচ্চি ছেলে",
            userAvatar = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200",
            userHandle = "@picchi_boy",
            totalAdsSeen = 19,
            dailyAdsServed = 8,
            completedAdsWatched = 12,
            skippedAdsCount = 7,
            bannerAdsViewed = 10,
            interstitialAdsViewed = 9,
            lastAdSeenTime = "৩ ঘণ্টা আগে",
            recentAdLogs = listOf(
                "৫ সেকেন্ড পর অ্যাড কেটে স্কিপ করেছে",
                "ভিডিও বটম ব্যানার অ্যাড ইমপ্রেশন হয়েছে"
            )
        ),
        UserAdAnalytics(
            userId = "user_jamai",
            userName = "জামাই",
            userAvatar = "https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?w=200",
            userHandle = "@jamai_babu",
            totalAdsSeen = 29,
            dailyAdsServed = 10,
            completedAdsWatched = 22,
            skippedAdsCount = 7,
            bannerAdsViewed = 15,
            interstitialAdsViewed = 14,
            lastAdSeenTime = "৫ ঘণ্টা আগে",
            recentAdLogs = listOf(
                "রিলসে ভিডিও অ্যাড সম্পূর্ণ দেখেছে",
                "Interstitial অ্যাড দেখা সম্পন্ন"
            )
        )
    )

    // ১ কোটি ইউজারের মেসেনজার খারাপ ছবি, ভিডিও, ভয়েস ও আপত্তিকর চ্যাট অডিট রেকর্ড
    val initialFlaggedMessengerAudits = listOf(
        com.example.model.FlaggedMessengerAudit(
            id = "flag_1",
            senderUser = User(
                id = "usr_bad_actor_1",
                fullName = "রকি হাসান (ফেক আইডি)",
                username = "rocky_fake_99",
                avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
                country = "বাংলাদেশ 🇧🇩 (আইপি: ১৯২.১৬৮.১.১০৫)"
            ),
            recipientUser = friendNusrat,
            category = com.example.model.FlaggedMessageType.BAD_PHOTO,
            contentPreview = "🔞 আপত্তিকর ছবি ইনবক্সে পাঠানো হয়েছে (অটো ফিল্টারে আটকানো হয়েছে)",
            mediaUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=600",
            timestamp = "এইমাত্র (১০ মিনিট আগে)",
            aiSafetyDetection = "AI ৯৯.৪% ক্ষতিকর / আপত্তিকর অ্যাডাল্ট কনটেন্ট শনাক্ত করেছে",
            severity = "অত্যন্ত ক্ষতিকর (Critical)",
            status = com.example.model.FlaggedAuditStatus.PENDING
        ),
        com.example.model.FlaggedMessengerAudit(
            id = "flag_2",
            senderUser = User(
                id = "usr_bad_actor_2",
                fullName = "সাইবার স্পামার জাহিদ",
                username = "zahid_spammer",
                avatarUrl = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=200",
                country = "বাংলাদেশ 🇧🇩 (ঢাকা, মিরপুর)"
            ),
            recipientUser = userRahima,
            category = com.example.model.FlaggedMessageType.BAD_VIDEO,
            contentPreview = "🔞 অশ্লীল ভিডিও ক্লিপ পাঠিয়ে ব্ল্যাকমেইলের চেষ্টা",
            mediaUrl = "https://images.unsplash.com/photo-1536240478700-b869070f9279?w=600",
            timestamp = "২৫ মিনিট আগে",
            aiSafetyDetection = "AI ৯৭.৮% আপত্তিকর ভিডিও ফুটেজ শনাক্ত করেছে",
            severity = "উচ্চ ঝুঁকি (High)",
            status = com.example.model.FlaggedAuditStatus.PENDING
        ),
        com.example.model.FlaggedMessengerAudit(
            id = "flag_3",
            senderUser = User(
                id = "usr_bad_actor_3",
                fullName = "অজ্ঞাত হামলাকারী",
                username = "unknown_threat",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
                country = "বাংলাদেশ 🇧🇩 (চট্টগ্রাম)"
            ),
            recipientUser = friendTanvir,
            category = com.example.model.FlaggedMessageType.ABUSIVE_VOICE,
            contentPreview = "🎤 অডিওতে চরম গালিগালাজ ও প্রাণনাশের হুমকি রেকর্ড হয়েছে (১৮ সেকেন্ড)",
            isAudioVoice = true,
            voiceDurationSeconds = 18,
            timestamp = "১ ঘণ্টা আগে",
            aiSafetyDetection = "AI ভয়েস অ্যানালাইসিস: চরম অপভাষা ও হুমকি ডিটেক্ট করেছে",
            severity = "উচ্চ ঝুঁকি (High)",
            status = com.example.model.FlaggedAuditStatus.PENDING
        ),
        com.example.model.FlaggedMessengerAudit(
            id = "flag_4",
            senderUser = User(
                id = "usr_bad_actor_4",
                fullName = "জুয়াড়ি ও প্রতারক রাসেল",
                username = "gambler_russel",
                avatarUrl = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=200",
                country = "বাংলাদেশ 🇧🇩 (বরিশাল)"
            ),
            recipientUser = friendSamrat,
            category = com.example.model.FlaggedMessageType.HARMFUL_TEXT,
            contentPreview = "\"তোর অ্যাকাউন্টে লিঙ্ক পাঠাচ্ছি, এক্ষুনি ৫০ হাজার টাকা দে নয়তো তোর ছবি ফেসবুকে ভাইরাল করে দেব!\"",
            timestamp = "২ ঘণ্টা আগে",
            aiSafetyDetection = "AI ৯৮.২% চাঁদাবাজি ও সাইবার হেনস্তা ডিটেক্ট করেছে",
            severity = "অত্যন্ত ক্ষতিকর (Critical)",
            status = com.example.model.FlaggedAuditStatus.PENDING
        )
    )
}


