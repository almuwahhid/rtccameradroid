/*
 * Copyright 2014 Pierre Chabardes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.almuwahhid.isecurityrtctest.GTRTC;

import android.content.Context;
import android.util.Log;

import com.almuwahhid.isecurityrtctest.Candidate;
import com.almuwahhid.isecurityrtctest.Payload;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
//import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;

public class WebRtcClient {
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    private final static String TAG = "WebRtcClient";
    private final static int MAX_PEER = 2;
    private boolean[] endPoints = new boolean[MAX_PEER];
    private PeerConnectionFactory factory;
    private Peer peer = null;
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
    private PeerConnectionClient.PeerConnectionParameters mPeerConnParams;
    private MediaConstraints mPeerConnConstraints = new MediaConstraints();
    private MediaStream mLocalMediaStream;
    private VideoSource mVideoSource;
    private RtcListener mListener;
    VideoCapturer videoCapturer;
//    MessageHandler messageHandler = new MessageHandler();
    Context mContext;

    private CreateAnswerCommand answerCommand = new CreateAnswerCommand();
    private SetRemoteSDPCommand remoteSDPCommand = new SetRemoteSDPCommand();
    private AddIceCandidateCommand iceCandidateCommand = new AddIceCandidateCommand();

    /**
     * Implement this interface to be notified of events.
     */
    public interface RtcListener {
        void onCallReady(String type, String sdp);

        void onCandidateCall(int label, String id, String candidate);

        void onStatusChanged(String newStatus);

        void onLocalStream(MediaStream localStream);

        void onAddRemoteStream(MediaStream remoteStream);

        void onRemoveRemoteStream();
    }

    public interface Command {
        void execute(String peerId, JSONObject payload) throws JSONException;
    }

    public class CreateOfferCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Log.d(TAG, "CreateOfferCommand");
            peer.pc.createOffer(peer, mPeerConnConstraints);
        }
    }

    public class CreateAnswerCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Log.d(TAG, "CreateAnswerCommand");
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.optString("type")),
                    payload.optString("sdp")
            );
            peer.pc.setRemoteDescription(peer, sdp);
            peer.pc.createAnswer(peer, mPeerConnConstraints);
        }
    }

    public class SetRemoteSDPCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Log.d(TAG, "SetRemoteSDPCommand");
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.optString("type")),
                    payload.optString("sdp")
            );
            peer.pc.setRemoteDescription(peer, sdp);
        }
    }

    public class AddIceCandidateCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Log.d(TAG, "AddIceCandidateCommand");
            PeerConnection pc = peer.pc;
            if (pc.getRemoteDescription() != null) {
                IceCandidate candidate = new IceCandidate(
                        payload.optString("id"),
                        payload.optInt("label"),
                        payload.optString("candidate")
                );
                pc.addIceCandidate(candidate);
            }
        }
    }

    /**
     * Send a message through the signaling server
     *
     * @param to      id of recipient
     * @param type    type of message
     * @param payload payload of message
     * @throws JSONException
     */
    public void sendMessage(String to, String type, JSONObject payload) throws JSONException {
        JSONObject message = new JSONObject();
        message.put("to", to);
        message.put("type", type);
        message.put("payload", payload);
        Log.d(TAG, "socket send " + type + " to " + to + " payload:" + payload);
    }

    public class Peer implements SdpObserver, PeerConnection.Observer {
        public PeerConnection pc;
        public String id;
        public int endPoint;

        @Override
        public void onCreateSuccess(final SessionDescription sdp) {
            pc.setLocalDescription(Peer.this, sdp);
        }

        @Override
        public void onSetSuccess() {
        }

        @Override
        public void onCreateFailure(String s) {
        }

        @Override
        public void onSetFailure(String s) {
        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        }


        public void onIceConnectionReceivingChange(boolean var1) {

        }

        public void onIceCandidatesRemoved(IceCandidate[] var1) {

        }

//        public void onAddTrack(RtpReceiver var1, MediaStream[] var2) {
//
//        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
                removePeer(id);
                mListener.onStatusChanged("DISCONNECTED");
            }
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        }

        @Override
        public void onIceCandidate(final IceCandidate candidate) {
            try {
                JSONObject payload = new JSONObject();
                payload.put("label", candidate.sdpMLineIndex);
                payload.put("id", candidate.sdpMid);
                payload.put("candidate", candidate.sdp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            Log.d(TAG, "onAddStream " + mediaStream.label());
            // remote streams are displayed from 1 to MAX_PEER (0 is localStream)
//            mediaStream.videoTracks.get(0).addRenderer(new VideoRenderer(mRemoteRender));
//            mListener.onAddRemoteStream(mediaStream, endPoint + 1);
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            Log.d(TAG, "onRemoveStream " + mediaStream.label());
            removePeer(id);
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
        }

        @Override
        public void onRenegotiationNeeded() {

        }

        public Peer() {
            this.pc = factory.createPeerConnection(iceServers, mPeerConnConstraints, this);
            pc.addStream(mLocalMediaStream); //, new MediaConstraints()
        }
    }

//    private Peer addPeer(String id, int endPoint) {
//        Peer peer = new Peer(id, endPoint);
//        peers.put(id, peer);
//
//        endPoints[endPoint] = true;
//        return peer;
//    }

    private void removePeer(String id) {
        peer.pc.close();
    }

    public WebRtcClient(Context context, RtcListener listener, VideoCapturer capturer, PeerConnectionClient.PeerConnectionParameters params) {
        mContext = context;
        mListener = listener;
        mPeerConnParams = params;
        videoCapturer = capturer;

//        PeerConnectionFactory.initializeAndroidGlobals(mContext, true, true, params.videoCodecHwAcceleration);
        factory = new PeerConnectionFactory();
        String host = "https://zainrtc.herokuapp.com/";
        Log.d(TAG, "socket start connect");

        iceServers.add(new PeerConnection.IceServer("stun:23.21.150.121"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));

        mPeerConnConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        mPeerConnConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        mPeerConnConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));

        peer = new Peer();

        initScreenCapturStream();
    }


    /**
     * Call this method in Activity.onDestroy()
     */
    public void destroy() {
        peer.pc.dispose();
        if (factory != null) {
            factory.dispose();
        }
        if (mVideoSource != null) {
            mVideoSource.dispose();
        }
    }

    private int findEndPoint() {
        for (int i = 0; i < MAX_PEER; i++) if (!endPoints[i]) return i;
        return MAX_PEER;
    }

    /**
     * Start the mSocket.
     * <p>
     * Set up the local stream and notify the signaling server.
     * Call this method after onCallReady.
     *
     * @param
     */

    public void answerCandidate(Candidate candidate){
        try {
            iceCandidateCommand.execute("cccf2335-66bf-11e2-ffed-e7f9e438c5b8", new JSONObject().put("candidate", candidate.getIce().getCandidate()).put("label", candidate.getIce().getSdpMLineIndex()).put("id", candidate.getIce().getSdpMid()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void answerOffer(Payload payload){
        try {
            answerCommand.execute("cccf2335-66bf-11e2-ffed-e7f9e438c5b8", new JSONObject().put("sdp", payload.getSdp().getSdp()).put("type", payload.getSdp().getType()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void answerAnswer(Payload payload){
        try {
            remoteSDPCommand.execute("cccf2335-66bf-11e2-ffed-e7f9e438c5b8", new JSONObject().put("sdp", payload.getSdp().getSdp()).put("type", payload.getSdp().getType()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initScreenCapturStream() {
        mLocalMediaStream = factory.createLocalMediaStream("ARDAMS");
        MediaConstraints videoConstraints = new MediaConstraints();
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", Integer.toString(mPeerConnParams.videoHeight)));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", Integer.toString(mPeerConnParams.videoWidth)));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(mPeerConnParams.videoFps)));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(mPeerConnParams.videoFps)));

//        VideoCapturer capturer = createScreenCapturer();
//        mVideoSource = factory.createVideoSource(videoCapturer);
//        videoCapturer.startCapture(mPeerConnParams.videoWidth, mPeerConnParams.videoHeight, mPeerConnParams.videoFps);
        VideoTrack localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, mVideoSource);
        localVideoTrack.setEnabled(true);
        mLocalMediaStream.addTrack(factory.createVideoTrack("ARDAMSv0", mVideoSource));
        AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
        mLocalMediaStream.addTrack(factory.createAudioTrack("ARDAMSa0", audioSource));
//        mLocalMediaStream.videoTracks.get(0).addRenderer(new VideoRenderer(mLocalRender));
//        mListener.onLocalStream(mLocalMediaStream);
        mListener.onStatusChanged("STREAMING");
    }


}
