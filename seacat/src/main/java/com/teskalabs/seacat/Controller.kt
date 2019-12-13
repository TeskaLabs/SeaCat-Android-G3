package com.teskalabs.seacat

open class Controller {

    open fun enroll(seacat: SeaCat) {
        // You may decide to call enroll() later, when you have more info
        seacat.identity.enroll()
    }

}