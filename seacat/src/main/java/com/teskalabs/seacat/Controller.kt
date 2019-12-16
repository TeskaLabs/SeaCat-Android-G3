package com.teskalabs.seacat

open class Controller {

    open fun onIntialEnrollmentRequested(seacat: SeaCat) {
        // You may decide to call seacat.identity.enroll() later, when you have more info
        seacat.identity.enroll()
    }

    open fun onReenrollmentRequested(seacat: SeaCat) {
        // You may decide to call seacat.identity.enroll() later, when you have more info
        seacat.identity.enroll()
    }

}