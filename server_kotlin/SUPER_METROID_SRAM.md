-------------------------------------------------------------------------------
| Super Metroid SRAM Document 1.0
| by John David Ratliff
|
| The most recent version of this guide can always be found at
| http://games.technoplaza.net/smse/sram-doc.txt
|
| Copyright (C) 2005 emuWorks (http://games.technoplaza.net/)
|   Permission is granted to copy, distribute and/or modify this document
|   under the terms of the GNU Free Documentation License, Version 1.2
|   or any later version published by the Free Software Foundation;
|   with no Invariant Sections, no Front-Cover Texts, and no Back-Cover
|   Texts.  A copy of the license can be found at
|   http://www.gnu.org/licenses/fdl.html
-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
| Table of Contents
-------------------------------------------------------------------------------

- 1.0 Introduction
- 2.0 Copyright Notice
- 3.0 Revision History
- 4.0 The Super Metroid SRAM
    - 4.1 SRAM Basics
    - 4.2 SRAM Offsets
    - 4.3 The Sanity Algorithm
    - 4.4 Checksum Repair Program (sanity.c)
- 5.0 smse - The Super Metroid SRAM Editor
- 6.0 Credits & Acknowledgements
- 7.0 Contact Information

-------------------------------------------------------------------------------
| 1.0 Introduction
-------------------------------------------------------------------------------

This document is a guide to the SRAM format used by Super Metroid for the
Super Nintendo and Super Famicom. It is not a walkthrough or guide to
anything else about the game or any other topic.

Save RAM, abbreviated SRAM, was used in nearly all Nintendo games to save
progress in the cartridge prior to the advent of memory cards and built-in
hard drives that came with third-generation (and later) systems such as the
Playstation and X-Box. SRAM is a special segment of memory which is preserved
using a battery built into the cartridge, and enables games to keep a limited
area of memory from being erased when the console (SNES) was turned off.

SRAM was reliable so long as the battery lasted, and the batteries were
generally guaranteed for 5 years from the date of cartridge production, and
often outlasted their guarantee period. Many Nintendo and Super Nintendo
cartridges with SRAM still have working batteries, and many others are now
dead.

Why have I spent two paragraphs talking about SRAM? Because it was the
storage medium used to save game progress for Super Metroid. All three games,
along with some other information was stored in a block of memory 0x2000
bytes long. (Note: all numbers prefixed with 0x are in the hexadecimal
system)

This document is a guide to what is stored in the SRAM and where. It is
useful only if you wish to alter the SRAM in some way. See section 5.0 for
a program that helps edit Super Metroid SRAM files.

Why would you want to edit the SRAM file? To edit your games; either to cheat
or to try things out. For example, say you wanted to try a new way of
beating Draygon, but didn't have any save games. Rather than play all the
way through to Draygon, you might just start a game, get past the space
colony, save the game on Zebes and edit the SRAM to put you near Draygon
with whatever equipment you might need to try your new method. Of course,
cheating is probably the more popular use for SRAM editing.

-------------------------------------------------------------------------------
| 2.0 Copyright Notice
-------------------------------------------------------------------------------

This document is Copyright (C) 2005 emuWorks (http://games.technoplaza.net/)
Permission is granted to copy, distribute and/or modify this document
under the terms of the GNU Free Documentation License, Version 1.2
or any later version published by the Free Software Foundation;
with no Invariant Sections, no Front-Cover Texts, and no Back-Cover
Texts.  A copy of the license can be found at
http://www.gnu.org/licenses/fdl.html

Basically, it is free documentation in much the same way software under the
GNU General Public License is free software. You can modify it, redistribute
it, sell it, publish it, etc.
  
-------------------------------------------------------------------------------
| 3.0 Revision History
-------------------------------------------------------------------------------

Version 1.0 - Saturday, October 15, 2005
- First Public Release
    
-------------------------------------------------------------------------------
| 4.0 The Super Metroid SRAM
-------------------------------------------------------------------------------

    This section details the SRAM format used by Super Metroid.

-------------------------------------------------------------------------------
| 4.1 SRAM Basics
-------------------------------------------------------------------------------

    As was discused in section 1.0, Super Metroid used an SRAM of 0x2000 bytes
    to store game progress.
    
    Each game occupied 0x65C bytes of the SAM for a total of 0x1314 bytes.
    There are 8 bytes of meta-data kept on each game for a total of 0x18 bytes,
    bringing us to a total of 0x132C bytes. The remaining 0xCD4 bytes are
    largely unused. There are a few game properties that are stored in the
    SRAM, but these are not documented here (nor do I know any of them).
    
    Because the SRAM is simply a block of RAM, it can lose its value if there
    is no power. The battery in the cartridge feeds power to the memory when
    the console is off, but if the battery dies, the SRAM will cease to
    function. Because the battery must eventually die, nearly every game that
    used SRAM added a sanity algorithm to confirm that the SRAM was still in
    working order. Data within the SRAM acts to confirm that the remainder of
    the SRAM data is valid. 8 bytes of SRAM data for each game are reserved for
    this purpose. They are the checksum (2 bytes), the checksum compliment
    (2 bytes), the redundant checksum (2 bytes), and the redundant checksum
    compliment (2 bytes). The sanity algorithm will be discussed in detail in
    section 4.4.
    
    The SRAM is largely composed of game data, the majority of which is of
    little importance for most purposes. There are only around 300 bits of
    really important data. These include the majority of Samus' progress,
    including her current and max energy and reserve energy, her current and
    max inventory (missiles, super missiles, and power bombs), which bosses
    have been defeated, the save point, the area maps Samus has downloaded,
    the game time, the controller and game configuration, which items Samus
    has found (morphing ball, screw attack, ice beam, etc.), which missile,
    super missile, and power bomb packs Samus has found, which energy and
    reserve tanks Samus has found, and which doors Samus has opened. The
    majority of the remaining data in the SRAM corresponds to Samus' personal
    map for each area, which is a listing of all the rooms Samus has entered in
    each area. The specifics of this personal map data are of little interest
    to me, and so are undocumented (and unknown).
    
    The data documented here is generally edited in a single bit of the SRAM.
    The next section will present a listing of all the known offsets and
    which bit within the offset is used.
    
    The three games, which are 0x65C bytes in length begin at offset 0x10 in
    the SRAM. SRAM offset 0x10 is the start of Game A. 0x66C is the start of
    Game B, and 0xCC8 is the start of Game C. The offset data in the next
    section is all relative to the start of each game. So offset 0x60 in game 1
    is really 0x70 from the start of the SRAM, 0x6CC in game 2, and 0xD28 in
    game 3.

-------------------------------------------------------------------------------
| 4.2 SRAM Offsets
-------------------------------------------------------------------------------

These offsets will be presented in no special order other than similar things
will be grouped together.
  
---------------------------------------------------------------------------

The energy and reserve energy are words (2 bytes). Words are special in the
SRAM because they must be stored in little endian format. If you don't know
what the is, you can google for it if you're interested.

Here is what it means for our purposes. It means you need to take the value
you want, convert it to hex, and reverse the bytes. For example, 1499, the
max energy is 0x5DB in hex. This word becomes the two bytes, 0x5 and 0xDB.
If you want to change the current energy to 1499, you need to put 0xDB in
offset 0x20 and 0x5 in offset 0x21. This looks backwards to us, but is
the way the 65c816 processor (used in the Super Nintendo/Famicom) reads
numbers.
  
---------------------------------------------------------------------------

Energy - Max energy is 1499 (0x5DB)
0x20 (Current)
0x22 (Max)

Reserve Energy - Max reserve energy is 400 (0x190)
0x34 (Current)
0x32 (Max)
    
---------------------------------------------------------------------------

Missiles - Max missiles is 230 (0xE6)
0x24 (Current)
0x26 (Max)

Super Missiles - Max super missiles is 50 (0x32)
0x28 (Current)
0x2A (Max)

Power Bombs - Max power bombs is 50 (0x32)
0x2C (Current)
0x2E (Max)
    
---------------------------------------------------------------------------

The next group of offsets detail Samus' equipment (morphing ball, screw
attack, ice beam, etc). The first offset is whether the item has been
taken, and therefore will not appear in the game. The second one is whether
Samus has the item in her inventory or not. The third one is whether the
item is equipped or not.

The grappling beam and the X-Ray scope are special in that you must have
them in the inventory and be equipped for Samus to be able to use them. If
they are not equipped, they won't show up or be selectable.

These offsets, as well as almost all others, are specified in the format
offset:bit where the offset is the byte location from the start of the
game data and the bit is the bit number within the byte, from 0-7 starting
at the rightmost bit. For example, if you had a byte with the following
bit pattern: 10010110, then bit 0 is off and bit 7 is on.
  
---------------------------------------------------------------------------

Morphing Ball
0xB3:2, 0x2:2, 0x0:2

Bombs
0xB0:7, 0x3:4, 0x1:4

Spring Ball
0xC2:6, 0x2:1, 0x0:1

High Jump Boots
0xB6:5, 0x3:0, 0x1:0

Varia Suit
0xB6:0, 0x2:0, 0x0:0

Gravity Suit
0xC0:7, 0x2:5, 0x0:5

Speed Booster
0xB8:2, 0x3:5, 0x1:5

Space Jump
0xC3:2, 0x3:1, 0x1:1

Screw Attack
0xB9:7, 0x2:3, 0x0:3

Charge Beam
0xB2:7, 0x7:4, 0x5:4

Ice Beam
0xB6:2, 0x6:1, 0x4:1

Wave Beam
0xB8:4, 0x6:0, 0x4:0

Spazer
0xB5:2, 0x6:2, 0x4:2

Plasma Beam
0xC1:7, 0x6:3, 0x4:3

Grappling Beam
0xB7:4, 0x3:6, 0x1:6

X-Ray Scope
0xB4:6, 0x3:7, 0x1:7
    
---------------------------------------------------------------------------

The next set of bits list the bosses and mini-bosses in the game. For the
four major bosses (Kraid, Phantoon, Draygon, and Ridley), the first bit
is whether they have been defeated, and the second is whether their statue
has been broken in the Tourian Elevator room.
  
---------------------------------------------------------------------------

Silver Torizo
0x68:2

Spore Spawn
0x69:1

Kraid
0x69:0, 0x61:1

Phantoon
0x6B:0, 0x60:6

Botwoon
0x6C:1

Draygon
0x6C:0, 0x61:0

Crocomire
0x6A:1

Golden Torizo
0x6A:2

Ridley
0x6A:0, 0x60:7
    
---------------------------------------------------------------------------

The metroid rooms determine if one of the four rooms in Tourian with
metroids has been cleared or not. If the room is cleared, there will not
be metroids in it if you go back into that room.
  
---------------------------------------------------------------------------

Metroid Rooms
0x62:0
0x62:1
0x62:2
0x62:3
    
---------------------------------------------------------------------------

The Zebetites byte is a little confusing. First, only three bits are used,
and five values: 000, 001, 010, 011, and 100. Each value represents the
number of destroyed Zebetites, 0-4. You cannot specify an individual
Zebetite to destroy, only the total number destroyed.

To change the number, pick the zebetite number you want, left shift it by
3 and bitwise OR it into the offset byte. You must bitwise OR because there
are other bits in the same byte used for other purposes.
  
---------------------------------------------------------------------------

Zebetites
0x60
    
---------------------------------------------------------------------------

The next three bits are miscellaneous useful things I found.

Tourian Elevator: Whether the elevator to Tourian is present or not
Maridia Glass Tube Broken: Whether the glass tube is broken or not
Rescued Animals: Whether Samus rescued the Animals during her escape or not
  
---------------------------------------------------------------------------

Tourian Elevator
0x61:2

Maridia Glass Tube Broken
0x61:3

Rescued Animals
0x61:7
    
---------------------------------------------------------------------------

The missile packs, super missile packs, power bomb packs, energy tanks, and
reserve tanks determine whether the particular item has been taken or not.
These may be used in the item percent calculation at the end, but I haven't
tested this.

I do not know which bit corresponds to which item.
  
---------------------------------------------------------------------------

Crateria Missile Packs (8)
0xB0:1, 0xB0:2, 0xB0:3, 0xB0:4
0xB0:6, 0xB1:1, 0xB1:2, 0xB1:4

Brinstar Missile Packs (12)
0xB1:7, 0xB2:2, 0xB2:3, 0xB2:5
0xB2:6, 0xB3:1, 0xB3:4, 0xB4:2
0xB4:4, 0xB4:5, 0xB5:1, 0xB5:4

Norfair Missile Packs (15)
0xB6:1, 0xB6:3, 0xB6:6, 0xB6:7
0xB7:2, 0xB7:3, 0xB7:6, 0xB7:7
0xB8:0, 0xB8:1, 0xB8:3, 0xB8:6
0xB9:1, 0xB9:2, 0xB9:5

Wrecked Ship Missile Packs (3)
0xC0:0, 0xC0:2, 0xC0:3

Maridia Missile Packs (8)
0xC1:0, 0xC1:3, 0xC1:5, 0xC1:6
0xC2:0, 0xC2:2, 0xC2:4, 0xC2:7

Crateria Super Missile Packs (1)
0xB1:3

Brinstar Super Missile Packs (3)
0xB1:6, 0xB2:0, 0xB3:7

Norfair Super Missile Packs (1)
0xB8:7

Wrecked Ship Super Missile Packs (2)
0xC0:5, 0xC0:6

Maridia Super Missile Packs (3)
0xC1:1, 0xC1:4, 0xC2:5

Crateria Power Bomb Packs (1)
0xB0:0

Brinstar Power Bomb Packs (5)
0xB1:5, 0xB3:0, 0xB3:3, 0xB4:7,
0xB5:0

Norfair Power Bomb Packs (3)
0xB7:1, 0xB9:3, 0xB9:4

Maridia Power Bomb Packs (1)
0xC2:3

Crateria Energy Tanks (2)
0xB0:5, 0xB1:0

Brinstar Energy Tanks (5)
0xB3:5, 0xB3:6, 0xB4:1, 0xB4:3, 0xB5:3

Norfair Energy Tanks (4)
0xB6:4, 0xB7:0, 0xB9:6, 0xBA:0

Wrecked Ship Energy Tanks (1)
0xC0:4

Maridia Energy Tanks (2)
0xC1:2, 0xC3:0

Brinstar Reserve Tank
0xB2:1

Norfair Reserve Tank
0xB7:5

Wrecked Ship Reserve Tank
0xC0:1

Maridia Reserve Tank
0xC2:1
    
---------------------------------------------------------------------------

The door bits determine whether a door has been opened or not. I know some
doors, while others are unknown. I have put comments by all the doors I
know.
  
---------------------------------------------------------------------------

Crateria Red Doors (3)
0xF0:5 (Crateria Map)
0xF3:5 (Bombs)
0xF3:6 (Tourian Elevator)

Brinstar Red Doors (10)
0xF3:7
0xF4:0 (Brinstar Map)
0xF4:1
0xF4:2
0xF4:3
0xF4:6 (Brinstar Reserve Tank)
0xF5:2 (Spore Spawn)
0xF5:3
0xF6:2
0xF7:2 (X-Ray Scope)

Norfair Red Doors (7)
0xF9:2
0xF9:5 (High Jump Boots)
0xFA:1
0xFA:2
0xFA:5 (Speed Booster)
0xFA:6
0xFA:7 (Wave Beam)

Wrecked Ship Red Doors (1)
0x101:3 (Reserve Tank)

Maridia Red Doors (8)
0x101:4
0x101:5
0x101:6
0x102:0 (Maridia Map)
0x102:2
0x102:6
0x103:0

Tourian Red Doors (2)
0x104:7
0x105:1 (Mother Brain)

Crateria Green Doors (2)
0xF0:0
0xF1:4 (Wrecked Ship)

Brinstar Green Doors (10)
0xF5:1
0xF5:6 (Spore Spawn Exit)
0xF6:3
0xF6:4
0xF6:5
0xF7:0
0xF7:3
0xF7:5
0xF7:7 (Spazer)
0xF8:4

Norfair Green Doors (6)
0xF9:1
0xF9:3 (Ice Beam)
0xF9:6
0xFA:3
0xFA:4 (Speed Booster)
0xFB:7

Wrecked Ship Green Doors (1)
0x100:4

Maridia Green Doors (4)
0x101:7
0x102:4
0x102:5
0x103:2 (Draygon)

Crateria Yellow Doors (6)
0xF0:1
0xF1:5
0xF1:6
0xF1:7
0xF2:0
0xF2:3

Brinstar Yellow Doors (4)
0xF5:0
0xF6:0
0xF7:1 (X-Ray Scope)
0xF7:4

Norfair Yellow Doors (3)
0xF9:4 (Norfair Map)
0xFB:0
0xFB:6

Crateria Metal Doors (1)
0xF3:3 (Bombs Exit)

Brinstar Metal Doors (16)
0xF3:1 (Old Tourian Right)
0xF3:2 (Old Tourian Left)
0xF4:5 (Brinstar Map Exit)
0xF5:4
0xF5:5
0xF5:7
0xF6:1
0xF6:6
0xF6:7
0xF7:6
0xF8:0
0xF8:1
0xF8:2 (Mini-Kraid Right)
0xF8:3 (Mini-Kraid Left)
0xF8:6 (Varia Suit)
0xF8:7 (Kraid Exit)

Norfair Metal Doors (6)
0xF9:7 (Crocomire Exit)
0xFA:0 (High Jump Boots Exit)
0xFB:1 (Screw Attack)
0xFB:2 (Ridley Exit)
0xFB:3 (Ridley Left)
0xFC:0 (Gold Space Pirates)

Wrecked Ship Metal Doors (5)
0x100:3
0x100:6 (Phantoon Exit)
0x101:0
0x101:1
0x101:2

Maridia Metal Doors (7)
0x102:1 (Plasma Exit)
0x102:3 (Plasma)
0x102:7
0x103:4
0x103:5 (Botwoon Exit)
0x103:6 (Draygon Exit)
0x103:7 (Space Jump)

Tourian Metal Doors (5)
0x104:0
0x104:1
0x104:2
0x104:3
0x104:5 (Dust Chozo - Point of No Return)

Brinstar Eye Door (Kraid)
0xF8:5

Norfair Eye Door (Ridley)
0xFB:4

Wrecked Ship Eye Door (Phantoon)
0x100:5

Maridia Eye Door (Draygon)
0x103:3

Tourian Eye Door
0x105:0
    
---------------------------------------------------------------------------

The six area maps are simple. A value of 0 means you don't have the area
map, and a value of 0xFF means you do.
  
---------------------------------------------------------------------------

Crateria Map
0x148

Brinstar Map
0x149

Norfair Map
0x14A

Wrecked Ship Map
0x14B

Maridia Map
0x14C

Tourian Map
0x14D
    
---------------------------------------------------------------------------

The save point is slightly, but not overly complicated. The first offset
determines which area you are in. 0 = Crateria, 1 = Brinstar, 2 = Norfair,
3 = Wrecked Ship, 4 = Maridia, and 5 = Tourian. The second offset
determines which save point within the area you are at starting at.

There are 2 save points in Crateria, 5 in Brinstar, 6 in Norfair, 1 in the
Wrecked Ship, 4 in Maridia, and 2 in Tourian.
  
---------------------------------------------------------------------------

Save Spot
0x158 (Area)
0x156 (Point)
    
---------------------------------------------------------------------------

Game time determines how much time the game has been played for. The max
game time is 99 hours and 59 minutes, after which the game stops tracking
your time.
  
---------------------------------------------------------------------------

Game Time
0x3E (Hours)
0x3C (Minutes)

Shot Button
0x10
    
---------------------------------------------------------------------------

The controller configuration is slightly complicated. Each button is a
word (2 bytes), but you don't need to worry about converting to little
ending here, because each button value is simple.

A = 0x80, 0
B = 0, 0x80
X = 0x40, 0
Y = 0, 0x40
L = 0x20, 0
R = 0x10, 0
Select = 0, 0x20

Don't duplicate any buttons and remember that Angle Up/Down can only be
assigned to R/L. If you change this, the game will simply ignore it.
  
---------------------------------------------------------------------------

Jump Button
0x12

Dash Button
0x14

Item Cancel Button
0x16

Item Select Button
0x18

Angle Down Button
0x1A

Angle Up Button
0x1C
    
---------------------------------------------------------------------------

These last three bits are the other part of the game configuration. They
are pretty straightforward.
  
---------------------------------------------------------------------------

Language (English or Japanese)
0x40:0

Moon Walk (Off or On)
0x42:0

Icon Cancel (Manual or Automatic)
0x48:0

-------------------------------------------------------------------------------
| 4.3 The Sanity Algorithm
-------------------------------------------------------------------------------

The sanity algorithm, used to ensure the SRAM is still valid is not very
complex, but hard to calculate by hand. If you want to edit the SRAM, but
don't want to do sanity algorithm math, you can either use smse (see section
5.0) or edit the SRAM by hand and use my checksum repair program (see section
4.4) to fix the sanity values for you.

The sanity values are comprised of eight bytes. A primary checksum (2 bytes),
a checksum compliment (the checksum XOR 0xFF -- 2 bytes), and redundant
copies of each of these two values for a total of 8 bytes.

The game requires at least one checksum and one checksum compliment to be
correct for it to consider a game to be valid. If both checksums or both
compliments are invalid, the game is considered invalid and is not playable.

To calculate the checksum, you will need two counter variables. I will call
them low and high. Set the low and high counter to 0 initially. Now, take the
SRAM data for a single game and do the following:

    1. For the current byte, add it to the high counter. If the counter exceeds
       255, subtract 255 from the high counter and increment the low counter by
       1.
    2. Take the next byte and add it to the low counter. If the counter exceeds
       255, subtract 255 from the low counter. Do NOT increment the high
       counter.

Continue until you have added all the bytes (half to the high counter, and
half to the low counter). When you are done, you should have two bytes, the
high counter and the low counter. The high counter is the high byte of the
checksum word, and the low counter is the low byte of the checksum word.

For the compliment, simply take the XOR of 0xFF with each byte of the
checksum. The high and low byte XOR values becomes the compliment value.

The redundant values are simply copies of the checksum and the compliment.

The values should be put in the SRAM at the following offsets (these are from
the start of the SRAM).

0x0, 0x1 Checksum High/Low Bytes Game 1
0x8, 0x9 Compliment High/Low Bytes Game 1
0x1FF0, 0x1FF1 Checksum High/Low Bytes Game 1
0x1FF8, 0x1FF9 Compliment High/Low Bytes Game 1

Game 2 and 3 are the bytes following game 1.

-------------------------------------------------------------------------------
| 4.4 Checksum Repair Program (sanity.c)
-------------------------------------------------------------------------------

This is a small program in ANSI C that can be used to repair sanity values in
a Super Metroid SRAM file.

There are a couple minor problems with this program. First, no file
validation is performed. Any file with at least 0x2000 bytes will be
accepted and overwritten. Be carewhat what arguments you pass to this
program. Second, invalid games are fixed. This means games that were blank
will become valid games that are unplayable. Just be aware of this when you
start Super Metroid in Snes9x or ZSNES (or whatever SNES emulator you use).
If you see a game where Samus has 0 energy, the game is useless. Samus will
die as soon as the game begins.

This program is standard ANSI C and should compile on any operating system
with an ANSI C compiler. I have put a windows binary up at
http://games.technoplaza.net/smse/history/sanity.zip

To run the program, simply type ./sanity [SRAM file]. If you want to compile
the program and have gcc, you can do "gcc sanity.c -o sanity"

I don't think there are any bugs, but I tested it all of one time, so who
knows. Write me a letter if it doesn't work or compile or something.

/*
* Super Metroid SRAM Checksum Repair Program
* Copyright (C) 2005 emuWorks
* http://games.technoplaza.net/
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  */

#include <stdio.h>

void sanitize(char *sram) {
int game, i;
short high, low;
unsigned char value;
unsigned char hb, lb, hc, lc;
char *ptr;

    for (game = 0; game < 3; ++game) {
        ptr = (sram + 0x10 + (0x65C * game));
        high = 0;
        low = 0;
        
        for (i = 0; i < 0x65C; ++i) {
            value = ptr[i];
            
            if ((high += value) > 0xFF) {
                high &= 0xFF;
                ++low;
            }
            
            value = ptr[++i];
            
            if ((low += value) > 0xFF) {
                low &= 0xFF;
            }
        }
        
        hb = (unsigned char)high;
        lb = (unsigned char)low;
        hc = (hb ^ 0xFF);
        lc = (lb ^ 0xFF);
        
        sram[game * 2] = hb;
        sram[game * 2 + 1] = lb;
        
        sram[8 + game * 2] = hc;
        sram[8 + game * 2 + 1] = lc;
        
        sram[0x1FF0 + game * 2] = hb;
        sram[0x1FF0 + game * 2 + 1] = lb;
        
        sram[0x1FF8 + game * 2] = hc;
        sram[0x1FF8 + game * 2 + 1] = lc;
    }
}

int main(int argc, char **argv) {
FILE *f;
char sram[0x2000];

    if (argc != 2) {
        fprintf(stderr, "syntax: sanity [Super Metroid SRAM File]\n");
        
        return -1;
    }
    
    if ((f = fopen(argv[1], "rb")) == NULL) {
        fprintf(stderr,
            "error: unable to open Super Metroid SRAM file for reading\n");
        
        return -1;
    }
    
    if (fread(sram, 0x2000, 1, f) != 1) {
        fprintf(stderr, "error: invalid SRAM file\n");
        
        return -1;
    }
    
    fclose(f);
    
    sanitize(sram);
    
    if ((f = fopen(argv[1], "wb")) == NULL) {
        fprintf(stderr,
            "error: unable to open Super Metroid SRAM file for writing\n");
        
        return -1;
    }
    
    if (fwrite(sram, 0x2000, 1, f) != 1) {
        fprintf(stderr, "error: unable to write SRAM data\n");
        
        return -1;
    }
    
    fclose(f);
    
    printf("successfully fixed sanity data in file: [%s]\n", argv[1]);
    
    return 0;
}

-------------------------------------------------------------------------------
| 5.0 smse - The Super Metroid SRAM Editor
-------------------------------------------------------------------------------

If you really want to edit the SRAM, I recommend a program called 'smse', the
Super Metroid SRAM Editor. Not surprisingly, I wrote it.

It will edit any of the offsets I have outlined in this document an will
keep fix the sanity values for you so you don't need to. It's far simpler
than trying to edit by hand, unless you need to edit parts of the SRAM I
haven't documented.

If you want to try it out, head over to http://games.technoplaza.net/smse/.
It's free software under the GNU GPL, tested in Windows, Linux, and Mac OS X,
and is likely to run on almost any unix that support GTK+.
    
-------------------------------------------------------------------------------
| 6.0 Credits & Acknowledgements
-------------------------------------------------------------------------------

I want to thank phonymike for discovering and documenting the checksum
algorithm used by Super Metroid. In addition, I want to thank him for the
many offsets he discovered and documented in his original Metroid 3 editor.

-------------------------------------------------------------------------------
| 7.0 Contact Information
-------------------------------------------------------------------------------

The author (John Ratliff) can be contacted at
webmaster [AT] technoplaza [DOT] net. Replace as necessary.

I can also be reached via an online feedback form at
http://www.technoplaza.net/feedback.php
