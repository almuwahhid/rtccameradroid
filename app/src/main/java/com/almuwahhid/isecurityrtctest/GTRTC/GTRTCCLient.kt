package chat.rocket.android.call.GTRTC


import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraDevice
import android.media.MediaRecorder
import android.opengl.EGLContext
import android.os.Build
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.almuwahhid.isecurityrtctest.Candidate
import com.almuwahhid.isecurityrtctest.GTRTC.GTPeerConnectionParameters
import com.almuwahhid.isecurityrtctest.Payload
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.*
import java.util.*


class GTRTCCLient(ctx: Context, peerParam: GTPeerConnectionParameters, rtcListener: RTCListener, mEGLcontext: EGLContext?) :
    SurfaceHolder.Callback {


    private val TAG = ".iSecurityRTCClient"

    private val pcConstraints = MediaConstraints()
    private var localMS: MediaStream? = null
    private var remoteMS: MediaStream? = null
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
    private var videoCapturer : VideoCapturer? = null

    private var offerCommand = CreateOfferCommand()
    private var answerCommand = CreateAnswerCommand()
    private var remoteSDPCommand = SetRemoteSDPCommand()
    private var iceCandidateCommand = AddIceCandidateCommand()
    internal var gson: Gson? = null

    private var surfaceView: SurfaceView? = null
    private var mCamera: CameraDevice? = null
    private var windowManager: WindowManager? = null
    var mediaRecorder: MediaRecorder? = null
    var layoutParams: WindowManager.LayoutParams? = null


    private fun initRTC(){
//        iceServers.add(PeerConnection.IceServer("stun:203.217.188.252:3479"))
//        iceServers.add(PeerConnection.IceServer("turn:203.217.188.252:3478", "truns", "r@h4sia.stun"))

        iceServers.add(PeerConnection.IceServer("stun:stun-cjt.kemlu.go.id:5349"))
//        iceServers.add(PeerConnection.IceServer("turn:stun-cjt.kemlu.go.id:5349", "stun", "turn.r4hasi4"))

//        iceServers.add(PeerConnection.IceServer("turn:203.217.189.66:3478", "turnuser", "Polycom12#\$"))

        pcConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"))
        pcConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        pcConstraints.optional.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
    }

    init {
        this.context = ctx
        this.rtccLient = rtcListener
        this.params = peerParam

        /*windowManager = context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        surfaceView = SurfaceView(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            )
        } else {
            layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            )
        }

        layoutParams!!.gravity = Gravity.LEFT or Gravity.TOP
        windowManager!!.addView(surfaceView, layoutParams)
        surfaceView!!.holder.addCallback(this)*/

//        var glview_call = GLSurfaceView(context)
//        glview_call.setPreserveEGLContextOnPause(true)
//        glview_call.setKeepScreenOn(true)
//
//        VideoRendererGui.setView(glview_call){
//
//        }
//
//        val displaySize = Point()
//        windowManager!!.defaultDisplay.getSize(displaySize)

//        PeerConnectionFactory.initializeAndroidGlobals(rtccLient, true, true,
//            params!!.videoCodecHwAcceleration, true)
        PeerConnectionFactory.initializeAndroidGlobals(rtccLient, true, true,
            params!!.videoCodecHwAcceleration, mEGLcontext)
//        PeerConnectionFactory.initializeAndroidGlobals(rtccLient, true, true,
//            params!!.videoCodecHwAcceleration, pcConstraints)
        factory = PeerConnectionFactory()
        gson = Gson()
        initRTC()

    }

    public fun answerCandidate(candidate: Candidate){
        iceCandidateCommand.execute(JSONObject().put("candidate", candidate.ice.candidate).put("label", candidate.ice.sdpMLineIndex).put("id", candidate.ice.sdpMid))
    }

    public fun answerOffer(payload : Payload){
        answerCommand.execute(JSONObject().put("sdp", payload.sdp.sdp).put("type", payload.sdp.type))
    }

    fun offers(){
        offerCommand.execute(JSONObject())
    }

    public fun answerAnswer(payload : Payload){
        remoteSDPCommand.execute(JSONObject().put("sdp", payload.sdp.sdp).put("type", payload.sdp.type))
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
//                rtccLient!!.onStatusChanged("Menghubungkan")
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

    fun initPeer(){
        setCamera()
        peer = Peer()
//        offerCommand.execute(JSONObject())
    }

//    public fun initStream(model: GTCallModel){
    fun initStream(){
        if(peer == null){
            setCamera()
            peer = Peer()
//            peer!!.pc!!.addStream(localMS)
        }
    }

    private fun setCamera() {
        localMS = factory!!.createLocalMediaStream("ARDAMS")
        Log.d(TAG, "setCamera: " + localMS!!.label())
        if (params!!.videoCallEnabled) {

        }

        val videoConstraints = MediaConstraints()
        videoConstraints.mandatory.add(MediaConstraints.KeyValuePair("maxHeight", Integer.toString(params!!.videoHeight)))
        videoConstraints.mandatory.add(MediaConstraints.KeyValuePair("maxWidth", Integer.toString(params!!.videoWidth)))
        videoConstraints.mandatory.add(MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(params!!.videoFps)))
        videoConstraints.mandatory.add(MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(params!!.videoFps)))

        videoSource = factory!!.createVideoSource(getVideoCapturer(), videoConstraints)
        localMS!!.addTrack(factory!!.createVideoTrack("ARDAMSv0", videoSource))
        val audioSource = factory!!.createAudioSource(MediaConstraints())
//        localMS!!.addTrack(factory!!.createAudioTrack("ARDAMSa0", audioSource))

        rtccLient!!.onLocalStream(localMS!!)
    }

    private fun getVideoCapturer(): VideoCapturer {
        //            TODO : here
//        TODO("Not yet implemented")
        val frontCameraDeviceName = VideoCapturerAndroid.getNameOfBackFacingDevice()
        videoCapturer = VideoCapturerAndroid.create(frontCameraDeviceName)
        return videoCapturer!!
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
                context!!.sendBroadcast(Intent("iSecurity").putExtra("data", "oncreateSccess "+sdp.type.canonicalForm()+" "+sdp.description))
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
//            remoteMS = mediaStream
//            peer!!.pc!!.addStream(remoteMS)
//            rtccLient!!.onAddRemoteStream(mediaStream)
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

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun surfaceCreated(p0: SurfaceHolder?) {
        mediaRecorder = MediaRecorder()

        /*try {
            val manager =
                getSystemService<Any>(CAMERA_SERVICE) as CameraManager?
            val cameras = manager!!.cameraIdList
            val characteristics =
                manager!!.getCameraCharacteristics(cameras[1])
            val configs = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
            )
            val sizes: Array<out Size>? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    configs!!.getOutputSizes(MediaCodec::class.java)
                } else {
                    TODO("VERSION.SDK_INT < LOLLIPOP")
                }


            val sizeHigh: Size = sizes!![0]
            if (ActivityCompat.checkSelfPermission(
                    context!!,
                            Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            manager!!.openCamera(cameras[1], object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    mCamera = camera
                    mediaRecorder!!.setPreviewDisplay(p0!!.getSurface())
                    mediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
                    mediaRecorder!!.setMaxFileSize(0)
                    mediaRecorder!!.setOrientationHint(0)
                    try {
                        mediaRecorder!!.prepare()
                    } catch (ignored: Exception) {

                    }
                }

                override fun onDisconnected(camera: CameraDevice) {}
                override fun onError(camera: CameraDevice, error: Int) {}
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }*/

        TODO("Not yet implemented")
        PeerConnectionFactory.initializeAndroidGlobals(rtccLient, true, true,
            params!!.videoCodecHwAcceleration, true)
        factory = PeerConnectionFactory()
        gson = Gson()

    }
}