/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/zhaolaichao/project_code/Monster_NetManage/src/com/mst/tms/ITrafficCorrectListener.aidl
 */
package com.mst.tms;
public interface ITrafficCorrectListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.mst.tms.ITrafficCorrectListener
{
private static final java.lang.String DESCRIPTOR = "com.mst.tms.ITrafficCorrectListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.mst.tms.ITrafficCorrectListener interface,
 * generating a proxy if needed.
 */
public static com.mst.tms.ITrafficCorrectListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.mst.tms.ITrafficCorrectListener))) {
return ((com.mst.tms.ITrafficCorrectListener)iin);
}
return new com.mst.tms.ITrafficCorrectListener.Stub.Proxy(obj);
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
case TRANSACTION_onNeedSmsCorrection:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
this.onNeedSmsCorrection(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_onTrafficInfoNotify:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
int _arg2;
_arg2 = data.readInt();
int _arg3;
_arg3 = data.readInt();
this.onTrafficInfoNotify(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
return true;
}
case TRANSACTION_onError:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
this.onError(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.mst.tms.ITrafficCorrectListener
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
@Override public void onNeedSmsCorrection(int simIndex, java.lang.String queryCode, java.lang.String queryPort) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(simIndex);
_data.writeString(queryCode);
_data.writeString(queryPort);
mRemote.transact(Stub.TRANSACTION_onNeedSmsCorrection, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onTrafficInfoNotify(int simIndex, int trafficClass, int subClass, int kBytes) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(simIndex);
_data.writeInt(trafficClass);
_data.writeInt(subClass);
_data.writeInt(kBytes);
mRemote.transact(Stub.TRANSACTION_onTrafficInfoNotify, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onError(int simIndex, int errorCode) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(simIndex);
_data.writeInt(errorCode);
mRemote.transact(Stub.TRANSACTION_onError, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onNeedSmsCorrection = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onTrafficInfoNotify = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_onError = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public void onNeedSmsCorrection(int simIndex, java.lang.String queryCode, java.lang.String queryPort) throws android.os.RemoteException;
public void onTrafficInfoNotify(int simIndex, int trafficClass, int subClass, int kBytes) throws android.os.RemoteException;
public void onError(int simIndex, int errorCode) throws android.os.RemoteException;
}
