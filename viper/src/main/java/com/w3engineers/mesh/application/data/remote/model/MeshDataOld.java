package com.w3engineers.mesh.application.data.remote.model;

/*
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * ============================================================================
 */

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represent piece of data. Contains: type, sender/receiver and data in bytes
 */
public class MeshDataOld extends BaseMeshData implements Parcelable {

    /**
     * Type of Data. Value {@value ProfileManager#MY_PROFILE_INFO_TYPE}
     * is reserved for Profile Info type. Developers should not use this value.
     */
    public byte mType;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(this.mType);
    }

    public MeshDataOld() {
    }

    protected MeshDataOld(Parcel in) {
        super(in);
        this.mType = in.readByte();
    }

    public static final Parcelable.Creator<MeshDataOld> CREATOR = new Parcelable.Creator<MeshDataOld>() {
        @Override
        public MeshDataOld createFromParcel(Parcel source) {
            return new MeshDataOld(source);
        }

        @Override
        public MeshDataOld[] newArray(int size) {
            return new MeshDataOld[size];
        }
    };


    /*public static byte[] getMeshData(MeshDataOld meshData) {

        if(meshData == null || meshData.mData == null) {
            return null;
        }

        ByteBuffer buffer;
        String peerId = meshData.mPeerId;
        byte hasPeer;

        if (TextUtils.isEmpty(peerId)) {
            hasPeer = 0;
            byte dataLength = (byte) meshData.mData.length;

            buffer = ByteBuffer.allocate(1 + 1 + 1 + meshData.mData.length);
            buffer.put(meshData.mType);
            buffer.put(hasPeer);
            buffer.put(dataLength);
            buffer.put(meshData.mData);
        } else {

            hasPeer = 1;
            byte[] peerByte = peerId.getBytes();

            byte length = (byte) peerByte.length;
            byte dataLength = (byte) meshData.mData.length;

            buffer = ByteBuffer.allocate(1 + 1 + 1 + 1 + peerByte.length + meshData.mData.length);
            buffer.put(meshData.mType);
            buffer.put(hasPeer);
            buffer.put(length);
            buffer.put(dataLength);

            buffer.put(peerByte);
            buffer.put(meshData.mData);
        }

        return buffer.array();
    }

    public static byte[] getPingData(MeshDataOld meshData) {
        if (meshData == null)
            return null;

        ByteBuffer buffer;
        String peerId = meshData.mPeerId;
        byte hasPeer;

        if (TextUtils.isEmpty(peerId)) {
            hasPeer = 0;
            buffer = ByteBuffer.allocate(1 + 1 + 1);
            buffer.put(meshData.mType);
            buffer.put(hasPeer);
            buffer.put(hasPeer);
        } else {

            hasPeer = 1;
            byte[] peerByte = peerId.getBytes();

            byte length = (byte) peerByte.length;
            buffer = ByteBuffer.allocate(1 + 1 + 1 + 1 + peerByte.length);
            buffer.put(meshData.mType);
            buffer.put(hasPeer);
            buffer.put(length);
            buffer.put(hasPeer);
            buffer.put(peerByte);
        }

        return buffer.array();
    }



    public static MeshDataOld setMeshData(byte[] meshDataBytes) {

        if(meshDataBytes == null || meshDataBytes.length < 2) {
            throw new IllegalStateException("Corrupted data");
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(meshDataBytes);

        MeshDataOld meshData = new MeshDataOld();
        meshData.mType = byteBuffer.get();

        Log.v("MIMO_SAHA::", "Type: " + meshData.mType);

        byte hasPeer = byteBuffer.get();
        byte dataLength;

        if (hasPeer == 0) {
            dataLength = byteBuffer.get();
            if (byteBuffer.hasRemaining()) {
                byte[] peerData = new byte[dataLength];
                byteBuffer.get(peerData);

                meshData.mData = Arrays.copyOfRange(peerData, 0, peerData.length);
            }

        } else if (hasPeer == 1) {
            byte peerLength = byteBuffer.get();
            dataLength = byteBuffer.get();

            byte[] peerByte = new byte[peerLength];

            byteBuffer.get(peerByte);

            meshData.mPeerId = new String(peerByte);

            if (byteBuffer.hasRemaining()) {
                byte[] peerData = new byte[dataLength];
                byteBuffer.get(peerData);

                meshData.mData = Arrays.copyOfRange(peerData, 0, peerData.length);
            }
        }


        return meshData;
    }

    public MeshDataOld copy() {
        MeshDataOld meshData = new MeshDataOld();
        meshData.mType = mType;
        meshData.mData = mData;

        return meshData;
    }*/
}
