/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/ximoon/document/git/Monster_Music/cnLib/src/main/aidl/com/mixvibes/common/service/IMusicServiceCallback.aidl
 */
package com.mixvibes.common.service;
/**
 * Example of a callback interface used by IRemoteService to send
 * synchronous notifications back to its clients.  Note that this is a
 * one-way interface so the server does not block waiting for the client.
 */
public interface IMusicServiceCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.mixvibes.common.service.IMusicServiceCallback
{
private static final java.lang.String DESCRIPTOR = "com.mixvibes.common.service.IMusicServiceCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.mixvibes.common.service.IMusicServiceCallback interface,
 * generating a proxy if needed.
 */
public static com.mixvibes.common.service.IMusicServiceCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.mixvibes.common.service.IMusicServiceCallback))) {
return ((com.mixvibes.common.service.IMusicServiceCallback)iin);
}
return new com.mixvibes.common.service.IMusicServiceCallback.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_trackHasChanged:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
this.trackHasChanged(_arg0, _arg1, _arg2);
return true;
}
case TRANSACTION_stateHasChanged:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
this.stateHasChanged(_arg0);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.mixvibes.common.service.IMusicServiceCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
     * Called when the service has a new value for you.
     */
@Override public void trackHasChanged(java.lang.String title, java.lang.String artist, java.lang.String album) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(title);
_data.writeString(artist);
_data.writeString(album);
mRemote.transact(Stub.TRANSACTION_trackHasChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void stateHasChanged(boolean state) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((state)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_stateHasChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_trackHasChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_stateHasChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
/**
     * Called when the service has a new value for you.
     */
public void trackHasChanged(java.lang.String title, java.lang.String artist, java.lang.String album) throws android.os.RemoteException;
public void stateHasChanged(boolean state) throws android.os.RemoteException;
}
