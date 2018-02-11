package com.mst.thememanager.parser;

public interface ThemeParser<O,I> {
	public static final String ENCODING = "utf-8";
	public O parser(I in);
}
