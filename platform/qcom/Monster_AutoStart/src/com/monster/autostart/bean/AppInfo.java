/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.monster.autostart.bean;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppInfo{
    
    public static final int NO_ID = -1;
     
    public long id = NO_ID;
	
    public String title;
	
    /**0:disable,1:enable*/
    public int status = 0;
    
    public Intent intent; 
     
    public Drawable icon;
    
    public List<Intent> intents = new ArrayList<Intent>();
    
    public void setTitle(String l){
    	this.title = l;
    }
    
    public void setStatus(int s){
    	this.status = s;
    }
    
    public void setIntent(Intent i){
    	this.intent = i;
    }
    
    public String getTitle(){
    	return this.title;
    }
    
    public int getStatus(){
    	return this.status;
    }
    
    public Intent getIntent(){
    	return this.intent;
    }
    
    public void setIcon(Drawable d){
    	icon = d;
    }
    
    public Drawable getDrawable(){
    	return icon;
    }
    
    @Override
    public String toString() {
    	// TODO Auto-generated method stub
    	return "title="+getTitle()+";"+"status="+getStatus()+";"+"intent="+getIntent();
    }
    
    
    public void addIntent(Intent intent){
    	intents.add(intent);
    }
    
    public List<Intent>getIntents(){
    	return this.intents;
    }
    
}

