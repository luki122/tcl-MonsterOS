/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/liyang/code/london/packages/apps/Monster_Dialer/src/com/mst/tms/ITmsService.aidl
 */
package com.mst.tms;
public interface ITmsService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.mst.tms.ITmsService
{
private static final java.lang.String DESCRIPTOR = "com.mst.tms.ITmsService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.mst.tms.ITmsService interface,
 * generating a proxy if needed.
 */
public static com.mst.tms.ITmsService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.mst.tms.ITmsService))) {
return ((com.mst.tms.ITmsService)iin);
}
return new com.mst.tms.ITmsService.Stub.Proxy(obj);
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
case TRANSACTION_getArea:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.getArea(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_updateDatabaseIfNeed:
{
data.enforceInterface(DESCRIPTOR);
this.updateDatabaseIfNeed();
reply.writeNoException();
return true;
}
case TRANSACTION_getMark:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
com.mst.tms.MarkResult _result = this.getMark(_arg0, _arg1);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getUsefulNumber:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.util.List<com.mst.tms.UsefulNumberResult> _result = this.getUsefulNumber(_arg0);
reply.writeNoException();
reply.writeTypedList(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.mst.tms.ITmsService
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
@Override public java.lang.String getArea(java.lang.String number) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(number);
mRemote.transact(Stub.TRANSACTION_getArea, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void updateDatabaseIfNeed() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_updateDatabaseIfNeed, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public com.mst.tms.MarkResult getMark(int type, java.lang.String number) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.mst.tms.MarkResult _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(type);
_data.writeString(number);
mRemote.transact(Stub.TRANSACTION_getMark, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = com.mst.tms.MarkResult.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.util.List<com.mst.tms.UsefulNumberResult> getUsefulNumber(java.lang.String number) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<com.mst.tms.UsefulNumberResult> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(number);
mRemote.transact(Stub.TRANSACTION_getUsefulNumber, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArrayList(com.mst.tms.UsefulNumberResult.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getArea = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_updateDatabaseIfNeed = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getMark = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getUsefulNumber = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
public java.lang.String getArea(java.lang.String number) throws android.os.RemoteException;
public void updateDatabaseIfNeed() throws android.os.RemoteException;
public com.mst.tms.MarkResult getMark(int type, java.lang.String number) throws android.os.RemoteException;
public java.util.List<com.mst.tms.UsefulNumberResult> getUsefulNumber(java.lang.String number) throws android.os.RemoteException;
}
