/*
 * feinrip
 *
 * Copyright (C) 2014 Richard "Shred" Körber
 *   https://github.com/shred/feinrip
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.shredzone.feinrip.model;

/**
 * A chapter position and name.
 *
 * @author Richard "Shred" Körber
 */
public class Chapter {

    private int number;
    private String title;
    private String position;

    /**
     * Chapter number.
     */
    public int getNumber()                      { return number; }
    public void setNumber(int number)           { this.number = number; }

    /**
     * Chapter title.
     */
    public String getTitle()                    { return title; }
    public void setTitle(String title)          { this.title = title; }

    /**
     * Position. Syntax: "hh:mm:ss.nnn"
     */
    public String getPosition()                 { return position; }
    public void setPosition(String position)    { this.position = position; }

}
