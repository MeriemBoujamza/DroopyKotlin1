package ma.ensaf.veryempty.models

import java.io.Serializable

class Posts : Serializable {
    var id = 0
    var user: Users? = null
    var timeAgo: String? = null
    var postContent: String? = null
    var postImage: String? = null
    var isLiked = false

    constructor() {}
    constructor(id: Int, user: Users?, timeAgo: String?, postContent: String?, postImage: String?) {
        this.id = id
        this.user = user
        this.timeAgo = timeAgo
        this.postContent = postContent
        this.postImage = postImage
    }

    constructor(
        id: Int,
        user: Users?,
        timeAgo: String?,
        postContent: String?,
        postImage: String?,
        isLiked: Boolean
    ) {
        this.id = id
        this.user = user
        this.timeAgo = timeAgo
        this.postContent = postContent
        this.postImage = postImage
        this.isLiked = isLiked
    }
}