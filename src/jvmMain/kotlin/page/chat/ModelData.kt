package page.chat

data class ModelData(
    val `data`: List<Permission>,
    val `object`: String
)
data class Permission(
    val created: Int,
    val id: String,
    val `object`: String,
    val ownedBy: String,
    val parent: Any,
    val permission: List<PermissionX>,
    val root: String
)

data class PermissionX(
    val allowCreateEngine: Boolean,
    val allowFineTuning: Boolean,
    val allowLogprobs: Boolean,
    val allowSampling: Boolean,
    val allowSearchIndices: Boolean,
    val allowView: Boolean,
    val created: Int,
    val group: Any,
    val id: String,
    val isBlocking: Boolean,
    val `object`: String,
    val organization: String
)