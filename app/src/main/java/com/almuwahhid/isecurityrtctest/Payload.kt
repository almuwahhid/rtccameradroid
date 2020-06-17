package com.almuwahhid.isecurityrtctest

data class Payload(var sdp: Sdp) {
    data class Sdp(var uuid: String,
                       var type: String,
                       var sdp: String) {
    }
}