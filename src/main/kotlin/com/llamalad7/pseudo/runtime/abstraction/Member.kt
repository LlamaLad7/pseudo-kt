package com.llamalad7.pseudo.runtime.abstraction

class Member(val visibility: Visibility, val access: Access, var value: BaseObject) {
    fun isPublic() = visibility == Visibility.PUBLIC
    fun isWriteable() = access == Access.WRITEABLE
}