package chat.rocket.android.call.GTRTC

import android.content.Context
import android.opengl.EGLContext
import android.util.Log


import com.almuwahhid.isecurityrtctest.GTRTC.GTPeerConnectionParameters
import com.google.gson.Gson

import org.json.JSONException
import org.json.JSONObject
import org.webrtc.*
import java.util.*

class GTRTCCLient(val ctx: Context, peerParam: GTPeerConnectionParameters, mEGLcontext: EGLContext, rtcListener: RTCListener) {
    private val TAG = ".WebRTCCLient"

    private val pcConstraints = MediaConstraints()
    private var localMS: MediaStream? = null
    private var factory: PeerConnectionFactory? = null
    private val iceServers = LinkedList<PeerConnection.IceServer>()
    private var videoSource: VideoSource? = null
    private var rtc: IceCandidate? = null
    private var sdp: SessionDescription? = null
    private var pr: PeerConnection? = null
    private var commandMap: HashMap<String, Command>? = null
    private var context: Context? = null
    private var rtccLient: RTCListener? = null
    private var params: GTPeerConnectionParameters? = null
    private var peer : Peer? = null

    private var offerCommand = CreateOfferCommand()
    private var answerCommand = CreateAnswerCommand()
    private var remoteSDPCommand = SetRemoteSDPCommand()
    private var iceCandidateCommand = AddIceCandidateCommand()
    internal var gson: Gson? = null

    private fun initRTC(){
//        iceServers.add(PeerConnection.IceServer("stun:203.217.188.252:3479"))
//        iceServers.add(PeerConnection.IceServer("turn:203.217.188.252:3478", "truns", "r@h4sia.stun"))

        iceServers.add(PeerConnection.IceServer("stun:stun-cjt.kemlu.go.id:5349"))
        iceServers.add(PeerConnection.IceServer("turn:stun-cjt.kemlu.go.id:5349", "stun", "turn.r4hasi4"))

//        iceServers.add(PeerConnection.IceServer("turn:203.217.189.66:3478", "turnuser", "Polycom12#\$"))

        pcConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        pcConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        pcConstraints.optional.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
    }

    init {
        this.context = ctx
        this.rtccLient = rtcListener
        this.params = peerParam
        PeerConnectionFactory.initializeAndroidGlobals(rtcListener, true, true,
                params!!.videoCodecHwAcceleration, mEGLcontext)
        factory = PeerConnectionFactory()
        gson = Gson()

        initRTC()
    }


    public interface RTCListener{
        fun onCallReady(type: String, sdp: String)

        fun onCandidateCall(label: Int, id: String, candidate: String)

        fun onStatusChanged(newStatus: String)

        fun onLocalStream(localStream: MediaStream)

        fun onAddRemoteStream(remoteStream: MediaStream)

        fun onRemoveRemoteStream()

    }
    private interface Command {
        @Throws(JSONException::class)
        fun execute(payload: JSONObject)

    }
    private inner class CreateOfferCommand : Command {
        @Throws(JSONException::class)
        override fun execute(payload: JSONObject) {
            Log.d(TAG, "CreateOfferCommand")
            if(peer!= null){
                peer!!.pc!!.createOffer(peer, pcConstraints)
                rtccLient!!.onStatusChanged("Menghubungkan")
            }
        }

    }

    private inner class CreateAnswerCommand : Command {
        @Throws(JSONException::class)
        override fun execute(payload: JSONObject) {
            Log.d(TAG, "CreateAnswerCommand")
            val sdp = SessionDescription(
//                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    SessionDescription.Type.fromCanonicalForm("offer"),
                    payload.getString("sdp")
            )
            peer!!.pc!!.setRemoteDescription(peer, sdp)
            peer!!.pc!!.createAnswer(peer, pcConstraints)
            rtccLient!!.onStatusChanged("Menghubungkan")
        }
    }

    private inner class SetRemoteSDPCommand : Command {
        @Throws(JSONException::class)
        override fun execute(payload: JSONObject) {
            Log.d(TAG, "SetRemoteSDPCommand")
            val sdp = SessionDescription(
//                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    SessionDescription.Type.fromCanonicalForm("answer"),
                    payload.getString("sdp")
            )
            peer!!.pc!!.setRemoteDescription(peer, sdp)
        }
    }

    private inner class AddIceCandidateCommand : Command {
        @Throws(JSONException::class)
        override fun execute(payload: JSONObject) {
            Log.d(TAG, "AddIceCandidateCommand")
            if (peer!!.pc!!.getRemoteDescription() != null) {
                val candidate = IceCandidate(
                        payload.getString("id"),
                        payload.getInt("label"),
                        payload.getString("candidate")
                )
                peer!!.pc!!.addIceCandidate(candidate)
            }
        }
    }

    public fun initPeer(){
        setCamera()
        peer = Peer()
        offerCommand.execute(JSONObject())
    }

//    public fun initStream(model: GTCallModel){
    public fun initStream(){
        if(peer == null){
            setCamera()
            peer = Peer()
            peer!!.pc!!.addStream(localMS)
        }

        /*if(gson!!.fromJson(model.payload, GTCallModel.Payload::class.java).type == 2){
            remoteSDPCommand.execute(JSONObject().put("sdp", gson!!.fromJson(model.payload, GTCallModel.Payload::class.java).sdp).put("type", model.type))
        } else {
            when(model.type){
                "sdp" -> {
                    Log.d(TAG, "initStream here answer")
//                answerCommand.execute(JSONObject().put("sdp", model.payload.sdp).put("type", model.type))
                    answerCommand.execute(JSONObject().put("sdp", gson!!.fromJson(model.payload, GTCallModel.Payload::class.java).sdp).put("type", "offer"))
                }
                "answer" -> {
                    Log.d(TAG, "initStream here callback answer")
                    remoteSDPCommand.execute(JSONObject().put("sdp", gson!!.fromJson(model.payload, GTCallModel.Payload::class.java).sdp).put("type", model.type))
                }
                "candidate" -> {
                    Log.d(TAG, "initStream here candidate")
                    iceCandidateCommand.execute(JSONObject().put("id", gson!!.fromJson(model.payload, GTCallModel.Payload::class.java).sdpMid).put("label", gson!!.fromJson(model.payload, GTCallModel.Payload::class.java).sdpMLineIndex).put("candidate", gson!!.fromJson(model.payload, GTCallModel.Payload::class.java).sdp))
                }
            }
        }*/
    }

    private fun setCamera() {
        localMS = factory!!.createLocalMediaStream("ARDAMS")
        Log.d(TAG, "setCamera: " + localMS!!.label())
        if (params!!.videoCallEnabled) {
            val videoConstraints = MediaConstraints()
            videoConstraints.mandatory.add(MediaConstraints.KeyValuePair("maxHeight", Integer.toString(params!!.videoHeight)))
            videoConstraints.mandatory.add(MediaConstraints.KeyValuePair("maxWidth", Integer.toString(params!!.videoWidth)))
            videoConstraints.mandatory.add(MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(params!!.videoFps)))
            videoConstraints.mandatory.add(MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(params!!.videoFps)))

            videoSource = factory!!.createVideoSource(getVideoCapturer(), videoConstraints)
            localMS!!.addTrack(factory!!.createVideoTrack("ARDAMSv0", videoSource))
        }

        val audioSource = factory!!.createAudioSource(MediaConstraints())
        localMS!!.addTrack(factory!!.createAudioTrack("ARDAMSa0", audioSource))

        rtccLient!!.onLocalStream(localMS!!)
    }

    private fun getVideoCapturer(): VideoCapturer {
        val frontCameraDeviceName = VideoCapturerAndroid.getNameOfFrontFacingDevice()
        return VideoCapturerAndroid.create(frontCameraDeviceName)
    }


    private inner class Peer() : SdpObserver, PeerConnection.Observer {
        val pc: PeerConnection?

        init {
            this.pc = factory!!.createPeerConnection(iceServers, pcConstraints, this)
            pc!!.addStream(localMS) //, new MediaConstraints()

            Log.d(TAG, "Menghubungkan Peer:")
        }

        override fun onCreateSuccess(sdp: SessionDescription) {
            // TODO: modify sdp to use pcParams prefered codecs
            try {
                val payload = JSONObject()
                payload.put("type", sdp.type.canonicalForm())
                payload.put("sdp", sdp.description)
                Log.d(TAG, "oncreateSccess "+sdp.type.canonicalForm()+" "+sdp.description)
                rtccLient!!.onCallReady(sdp.type.canonicalForm(), sdp.description)
                pc!!.setLocalDescription(this@Peer, sdp)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }

        override fun onSetSuccess() {}

        override fun onCreateFailure(s: String) {}

        override fun onSetFailure(s: String) {}

        override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {}

        override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
            if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
                removePeer()
//                mListener.onStatusChanged("Panggilan berakhir")
            }
        }

        override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {}

        override fun onIceCandidate(candidate: IceCandidate) {
            try {
                rtccLient!!.onCandidateCall(candidate.sdpMLineIndex, candidate.sdpMid, candidate.sdp)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }

        override fun onAddStream(mediaStream: MediaStream) {
            Log.d(TAG, "onAddStream " + mediaStream.label())
            // remote streams are displayed from 1 to MAX_PEER (0 is localStream)
            rtccLient!!.onAddRemoteStream(mediaStream)
        }

        override fun onRemoveStream(mediaStream: MediaStream) {
            Log.d(TAG, "onRemoveStream " + mediaStream.label())
            removePeer()
        }

        override fun onDataChannel(dataChannel: DataChannel) {}

        override fun onRenegotiationNeeded() {

        }
    }

    private fun removePeer() {
//        mListener.onRemoveRemoteStream(peer.endPoint)
        if(peer!=null){
            peer!!.pc!!.close()
        }
    }

    public fun stopPeer(){
        peer!!.pc!!.dispose()

        if (videoSource != null) {
//            videoSource!!.stop()
            videoSource!!.dispose()
        }
    }
}