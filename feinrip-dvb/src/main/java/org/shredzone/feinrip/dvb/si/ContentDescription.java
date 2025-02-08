/*
 * feinrip
 *
 * Copyright (C) 2014 Richard "Shred" Körber
 *   https://codeberg.org/shred/feinrip
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.shredzone.feinrip.dvb.si;

/**
 * A description of a stream content.
 *
 * @author Richard "Shred" Körber
 */
public class ContentDescription {

    private int content1;
    private int content2;
    private int user1;
    private int user2;

    /**
     * Creates an empty {@link ContentDescription} object.
     */
    public ContentDescription() {

    }

    /**
     * Creates an {@link ContentDescription} object, using the given nibbles.
     *
     * @param nibbles
     *          Nibbles to pre-set the object with
     */
    public ContentDescription(int nibbles) {
        content1 = (nibbles >> 12) & 0x0F;
        content2 = (nibbles >> 8) & 0x0F;
        user1 = (nibbles >> 4) & 0x0F;
        user2 = nibbles & 0x0F;
    }

    /**
     * Returns content level 1 as decoded string. See table 28.
     *
     * @return String explaining content level 1, or {@code null} if undefined.
     */
    public String getContentLevel1() {
        switch (content1) {
            case 0x00: return "Undefined content";
            case 0x01: return "Movie/Drama";
            case 0x02: return "News/Current affairs";
            case 0x03: return "Show/Game show";
            case 0x04: return "Sports";
            case 0x05: return "Children's/Youth programmes";
            case 0x06: return "Music/Ballet/Dance";
            case 0x07: return "Arts/Culture (without music)";
            case 0x08: return "Social/Political issues/Economics";
            case 0x09: return "Educational/Science/Factual topics";
            case 0x0A: return "Leisure hobbies";
            case 0x0B: return "Special characteristics";
            case 0x0F: return "user defined";
            default: return null;
        }
    }

    /**
     * Returns content level 2 as decoded string. See table 28.
     *
     * @return String explaining content level 2, or {@code null} if undefined.
     */
    public String getContentLevel2() {
        if (content1 == 0x00 || content1 > 0x0B) {
            return getContentLevel1();

        } else if (content2 == 0x0F) {
            String t1 = getContentLevel1();
            if (t1 != null) {
                return t1 + " (user defined)";
            } else {
                return null;
            }

        } else if (content1 == 0x0B) {
            switch (content2) {
                case 0x00: return "original language";
                case 0x01: return "black and white";
                case 0x02: return "unpublished";
                case 0x03: return "live broadcast";
                case 0x04: return "plano-stereoscopic";
                default: return null;
            }

        } else if (content2 == 0x00) {
            return getContentLevel1() + " (general)";

        }

        switch ((content1 << 4) + content2) {
            case 0x11: return "detective/thriller";
            case 0x12: return "adventure/western/war";
            case 0x13: return "science fiction/fantasy/horror";
            case 0x14: return "comedy";
            case 0x15: return "soap/melodrama/folkloric";
            case 0x16: return "romance";
            case 0x17: return "serious/classical/religious/historical movie/drama";
            case 0x18: return "adult movie/drama";
            case 0x21: return "news/weather report";
            case 0x22: return "news magazine";
            case 0x23: return "documentary";
            case 0x24: return "discussion/interview/debate";
            case 0x31: return "game show/quiz/contest";
            case 0x32: return "variety show";
            case 0x33: return "talk show";
            case 0x41: return "special events (Olympic Games, World Cup, etc.)";
            case 0x42: return "sport magazines";
            case 0x43: return "football/soccer";
            case 0x44: return "tennis/squash";
            case 0x45: return "team sports (excluding football)";
            case 0x46: return "athletics";
            case 0x47: return "motor sport";
            case 0x48: return "water sport";
            case 0x49: return "winter sports";
            case 0x4A: return "equestrian";
            case 0x4B: return "martial sports";
            case 0x51: return "pre-school children's programmes";
            case 0x52: return "enternainment programmes for 6 to 14";
            case 0x53: return "enternainment programmes for 10 to 16";
            case 0x54: return "informational/educational/school programmes";
            case 0x55: return "cartoons/puppets";
            case 0x61: return "rock/pop";
            case 0x62: return "serious music/classical music";
            case 0x63: return "folk/traditional music";
            case 0x64: return "jazz";
            case 0x65: return "musical/opera";
            case 0x66: return "ballet";
            case 0x71: return "performing arts";
            case 0x72: return "fine arts";
            case 0x73: return "religion";
            case 0x74: return "popular culture/traditional arts";
            case 0x75: return "literature";
            case 0x76: return "film/cinema";
            case 0x77: return "experimental film/video";
            case 0x78: return "broadcasting/press";
            case 0x79: return "new media";
            case 0x7A: return "arts/culture magazines";
            case 0x7B: return "fashion";
            case 0x81: return "magazines/reports/documentary";
            case 0x82: return "economics/social advisory";
            case 0x83: return "remarkable people";
            case 0x91: return "nature/animals/environment";
            case 0x92: return "technology/natural sciences";
            case 0x93: return "medicine/physiology/psychology";
            case 0x94: return "foreign countries/expeditions";
            case 0x95: return "social/spiritual sciences";
            case 0x96: return "further education";
            case 0x97: return "languages";
            case 0xA1: return "tourism/travel";
            case 0xA2: return "handicraft";
            case 0xA3: return "motoring";
            case 0xA4: return "fitness and health";
            case 0xA5: return "cooking";
            case 0xA6: return "advertisement/shopping";
            case 0xA7: return "gardening";
            default: return null;
        }
    }

    public int getContent1()                    { return content1; }
    public void setContent1(int content1)       { this.content1 = content1; }

    public int getContent2()                    { return content2; }
    public void setContent2(int content2)       { this.content2 = content2; }

    public int getUser1()                       { return user1; }
    public void setUser1(int user1)             { this.user1 = user1; }

    public int getUser2()                       { return user2; }
    public void setUser2(int user2)             { this.user2 = user2; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getContentLevel1()).append(':').append(getContentLevel2());
        sb.append(" (").append(content1).append(':').append(content2).append(':').append(user1).append(':').append(user2).append(')');
        return sb.toString();
    }

}
