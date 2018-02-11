/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/chengrq/iuniProject/Aurora_PrivacyManage/code/src/com/privacymanage/service/IPrivacyManageService.aidl
 */
package com.monster.privacymanage.service;
public interface IPrivacyManageService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.monster.privacymanage.service.IPrivacyManageService
{
private static final java.lang.String DESCRIPTOR = "com.monster.privacymanage.service.IPrivacyManageService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.monster.privacymanage.service.IPrivacyManageService interface,
 * generating a proxy if needed.
 */
public static com.monster.privacymanage.service.IPrivacyManageService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.monster.privacymanage.service.IPrivacyManageService))) {
return ((com.monster.privacymanage.service.IPrivacyManageService)iin);
}
return new com.monster.privacymanage.service.IPrivacyManageService.Stub.Proxy(obj);
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
case TRANSACTION_setPrivacyNum:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
long _arg3;
_arg3 = data.readLong();
this.setPrivacyNum(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
return true;
}
case TRANSACTION_getCurrentAccount:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
com.monster.privacymanage.entity.AidlAccountData _result = this.getCurrentAccount(_arg0, _arg1);
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
case TRANSACTION_getAllAccountId:
{
data.enforceInterface(DESCRIPTOR);
long[] _result = this.getAllAccountId();
reply.writeNoException();
reply.writeLongArray(_result);
return true;
}
case TRANSACTION_resetPrivacyNumOfAllAccount:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
this.resetPrivacyNumOfAllAccount(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.monster.privacymanage.service.IPrivacyManageService
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
     * 设置指定模块的隐私数据的个数
     * @param pkgName 子模块所在的包名
     * @param className 子模块对应界面的类名
     * @param num 个数
     * @param accountId 
     */
@Override public void setPrivacyNum(java.lang.String pkgName, java.lang.String className, int num, long accountId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pkgName);
_data.writeString(className);
_data.writeInt(num);
_data.writeLong(accountId);
mRemote.transact(Stub.TRANSACTION_setPrivacyNum, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * 获取当前的账户信息
     * @param pkgName 子模块所在的包名
     * @param className 子模块对应界面的类名
     */
@Override public com.monster.privacymanage.entity.AidlAccountData getCurrentAccount(java.lang.String pkgName, java.lang.String className) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.monster.privacymanage.entity.AidlAccountData _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pkgName);
_data.writeString(className);
mRemote.transact(Stub.TRANSACTION_getCurrentAccount, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = com.monster.privacymanage.entity.AidlAccountData.CREATOR.createFromParcel(_reply);
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
/**
     * 获取所有隐私账户的id
     */
@Override public long[] getAllAccountId() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getAllAccountId, _data, _reply, 0);
_reply.readException();
_result = _reply.createLongArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * 重置指定模块所有隐私空间下的隐私个数
     * @param pkgName 子模块所在的包名
     * @param className 子模块对应界面的类名
     */
@Override public void resetPrivacyNumOfAllAccount(java.lang.String pkgName, java.lang.String className) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pkgName);
_data.writeString(className);
mRemote.transact(Stub.TRANSACTION_resetPrivacyNumOfAllAccount, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_setPrivacyNum = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getCurrentAccount = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getAllAccountId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_resetPrivacyNumOfAllAccount = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
/**
     * 设置指定模块的隐私数据的个数
     * @param pkgName 子模块所在的包名
     * @param className 子模块对应界面的类名
     * @param num 个数
     * @param accountId 
     */
public void setPrivacyNum(java.lang.String pkgName, java.lang.String className, int num, long accountId) throws android.os.RemoteException;
/**
     * 获取当前的账户信息
     * @param pkgName 子模块所在的包名
     * @param className 子模块对应界面的类名
     */
public com.monster.privacymanage.entity.AidlAccountData getCurrentAccount(java.lang.String pkgName, java.lang.String className) throws android.os.RemoteException;
/**
     * 获取所有隐私账户的id
     */
public long[] getAllAccountId() throws android.os.RemoteException;
/**
     * 重置指定模块所有隐私空间下的隐私个数
     * @param pkgName 子模块所在的包名
     * @param className 子模块对应界面的类名
     */
public void resetPrivacyNumOfAllAccount(java.lang.String pkgName, java.lang.String className) throws android.os.RemoteException;
}
