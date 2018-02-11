package com.mst.wallpaper.managerservices;


interface IWallpaperManagerServices{

boolean applySystemDesktopWallpaper(int wallpaperId);

boolean applyCustomDesktopWallpaper(String path);

boolean applySystemKeyguardWallpaper(String wallpaperName);

boolean applyCustomKeyguardWallpaper(String path);

}