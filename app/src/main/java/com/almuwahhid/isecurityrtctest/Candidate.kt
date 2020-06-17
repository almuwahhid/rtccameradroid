package com.almuwahhid.isecurityrtctest

data class Candidate(var ice : Detail, var uuid : String) {
    data class Detail(var candidate : String,
                        var sdpMid : String,
                        var sdpMLineIndex : String
                        )
}