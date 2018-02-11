/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/zhaolaichao/project_code/Monster_NetManage/src/com/mst/tms/ITmsService.aidl
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
case TRANSACTION_getAllProvinces:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<com.mst.tms.CodeNameInfo> _result = this.getAllProvinces();
reply.writeNoException();
reply.writeTypedList(_result);
return true;
}
case TRANSACTION_getCities:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.util.List<com.mst.tms.CodeNameInfo> _result = this.getCities(_arg0);
reply.writeNoException();
reply.writeTypedList(_result);
return true;
}
case TRANSACTION_getCarries:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<com.mst.tms.CodeNameInfo> _result = this.getCarries();
reply.writeNoException();
reply.writeTypedList(_result);
return true;
}
case TRANSACTION_getBrands:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.util.List<com.mst.tms.CodeNameInfo> _result = this.getBrands(_arg0);
reply.writeNoException();
reply.writeTypedList(_result);
return true;
}
case TRANSACTION_setConfig:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
java.lang.String _arg4;
_arg4 = data.readString();
int _arg5;
_arg5 = data.readInt();
int _result = this.setConfig(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_startCorrection:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _result = this.startCorrection(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_analysisSMS:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
int _result = this.analysisSMS(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getTrafficInfo:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int[] _result = this.getTrafficInfo(_arg0);
reply.writeNoException();
reply.writeIntArray(_result);
return true;
}
case TRANSACTION_trafficCorrectListener:
{
data.enforceInterface(DESCRIPTOR);
com.mst.tms.ITrafficCorrectListener _arg0;
_arg0 = com.mst.tms.ITrafficCorrectListener.Stub.asInterface(data.readStrongBinder());
this.trafficCorrectListener(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_updateSimInfo:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String[] _arg0;
_arg0 = data.createStringArray();
this.updateSimInfo(_arg0);
reply.writeNoException();
reply.writeStringArray(_arg0);
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
@Override public java.util.List<com.mst.tms.CodeNameInfo> getAllProvinces() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<com.mst.tms.CodeNameInfo> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getAllProvinces, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArrayList(com.mst.tms.CodeNameInfo.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.util.List<com.mst.tms.CodeNameInfo> getCities(java.lang.String provinceCode) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<com.mst.tms.CodeNameInfo> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(provinceCode);
mRemote.transact(Stub.TRANSACTION_getCities, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArrayList(com.mst.tms.CodeNameInfo.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.util.List<com.mst.tms.CodeNameInfo> getCarries() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<com.mst.tms.CodeNameInfo> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getCarries, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArrayList(com.mst.tms.CodeNameInfo.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.util.List<com.mst.tms.CodeNameInfo> getBrands(java.lang.String carryId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<com.mst.tms.CodeNameInfo> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(carryId);
mRemote.transact(Stub.TRANSACTION_getBrands, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArrayList(com.mst.tms.CodeNameInfo.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int setConfig(int simIndex, java.lang.String provinceId, java.lang.String cityId, java.lang.String carryId, java.lang.String brandId, int closingDay) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(simIndex);
_data.writeString(provinceId);
_data.writeString(cityId);
_data.writeString(carryId);
_data.writeString(brandId);
_data.writeInt(closingDay);
mRemote.transact(Stub.TRANSACTION_setConfig, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int startCorrection(int simIndex) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(simIndex);
mRemote.transact(Stub.TRANSACTION_startCorrection, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int analysisSMS(int simIndex, java.lang.String queryCode, java.lang.String queryPort, java.lang.String smsBody) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(simIndex);
_data.writeString(queryCode);
_data.writeString(queryPort);
_data.writeString(smsBody);
mRemote.transact(Stub.TRANSACTION_analysisSMS, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int[] getTrafficInfo(int simIndex) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(simIndex);
mRemote.transact(Stub.TRANSACTION_getTrafficInfo, _data, _reply, 0);
_reply.readException();
_result = _reply.createIntArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void trafficCorrectListener(com.mst.tms.ITrafficCorrectListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_trafficCorrectListener, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void updateSimInfo(java.lang.String[] simImsiArray) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStringArray(simImsiArray);
mRemote.transact(Stub.TRANSACTION_updateSimInfo, _data, _reply, 0);
_reply.readException();
_reply.readStringArray(simImsiArray);
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_getArea = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_updateDatabaseIfNeed = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getMark = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getUsefulNumber = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getAllProvinces = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getCities = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_getCarries = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_getBrands = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_setConfig = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_startCorrection = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_analysisSMS = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_getTrafficInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_trafficCorrectListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_updateSimInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
}
public java.lang.String getArea(java.lang.String number) throws android.os.RemoteException;
public void updateDatabaseIfNeed() throws android.os.RemoteException;
public com.mst.tms.MarkResult getMark(int type, java.lang.String number) throws android.os.RemoteException;
public java.util.List<com.mst.tms.UsefulNumberResult> getUsefulNumber(java.lang.String number) throws android.os.RemoteException;
public java.util.List<com.mst.tms.CodeNameInfo> getAllProvinces() throws android.os.RemoteException;
public java.util.List<com.mst.tms.CodeNameInfo> getCities(java.lang.String provinceCode) throws android.os.RemoteException;
public java.util.List<com.mst.tms.CodeNameInfo> getCarries() throws android.os.RemoteException;
public java.util.List<com.mst.tms.CodeNameInfo> getBrands(java.lang.String carryId) throws android.os.RemoteException;
public int setConfig(int simIndex, java.lang.String provinceId, java.lang.String cityId, java.lang.String carryId, java.lang.String brandId, int closingDay) throws android.os.RemoteException;
public int startCorrection(int simIndex) throws android.os.RemoteException;
public int analysisSMS(int simIndex, java.lang.String queryCode, java.lang.String queryPort, java.lang.String smsBody) throws android.os.RemoteException;
public int[] getTrafficInfo(int simIndex) throws android.os.RemoteException;
public void trafficCorrectListener(com.mst.tms.ITrafficCorrectListener listener) throws android.os.RemoteException;
public void updateSimInfo(java.lang.String[] simImsiArray) throws android.os.RemoteException;
}
