/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/zhaolaichao/project_code/Monster_NetManage/src/com/mst/tms/INetworkChangeCallBackListener.aidl
 */
package com.mst.tms;
public interface INetworkChangeCallBackListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.mst.tms.INetworkChangeCallBackListener
{
private static final java.lang.String DESCRIPTOR = "com.mst.tms.INetworkChangeCallBackListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.mst.tms.INetworkChangeCallBackListener interface,
 * generating a proxy if needed.
 */
public static com.mst.tms.INetworkChangeCallBackListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.mst.tms.INetworkChangeCallBackListener))) {
return ((com.mst.tms.INetworkChangeCallBackListener)iin);
}
return new com.mst.tms.INetworkChangeCallBackListener.Stub.Proxy(obj);
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
case TRANSACTION_onClosingDateReached:
{
data.enforceInterface(DESCRIPTOR);
this.onClosingDateReached();
reply.writeNoException();
return true;
}
case TRANSACTION_onDayChanged:
{
data.enforceInterface(DESCRIPTOR);
this.onDayChanged();
reply.writeNoException();
return true;
}
case TRANSACTION_onNormalChanged:
{
data.enforceInterface(DESCRIPTOR);
com.mst.tms.NetInfoEntity _arg0;
if ((0!=data.readInt())) {
_arg0 = com.mst.tms.NetInfoEntity.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onNormalChanged(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.mst.tms.INetworkChangeCallBackListener
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
@Override public void onClosingDateReached() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onClosingDateReached, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onDayChanged() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onDayChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onNormalChanged(com.mst.tms.NetInfoEntity networkInfoEntity) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((networkInfoEntity!=null)) {
_data.writeInt(1);
networkInfoEntity.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onNormalChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onClosingDateReached = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onDayChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_onNormalChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public void onClosingDateReached() throws android.os.RemoteException;
public void onDayChanged() throws android.os.RemoteException;
public void onNormalChanged(com.mst.tms.NetInfoEntity networkInfoEntity) throws android.os.RemoteException;
}
