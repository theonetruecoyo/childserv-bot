/* vim: set filetype=java: ts=4: sw=4: */
/*
  Copyright (c) 2020, kawaiyume.net
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
  ----------------------------------------------------------------------------
  10 november 2020
  ----------------------------------------------------------------------------
  NBTHelper.java
  ----------------------------------------------------------------------------
  <TAB> = 4 <space>
  ----------------------------------------------------------------------------
 */

package net.kawaiyume.childserv;

import org.jnbt.CompoundTag;
import org.jnbt.NBTInputStream;
import org.jnbt.NBTOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class helping use of nbt files
 *
 * @author iXô (ixo@kawaiyume.net)
 */
public final class NBTHelper
{
	private static final Logger LOGGER = LoggerFactory.getLogger(NBTHelper.class);

	private NBTHelper()
	{
		// utility class
	}

	public static boolean writeFile(final CompoundTag tag, final String fileName)
	{
		return writeFile(tag, new File(fileName));
	}

	public static boolean writeFile(final CompoundTag tag, final File file)
	{
		if(tag == null)
		{
			LOGGER.error("Can't write to file a null tag !");
			return false;
		}

		try
		{
			final NBTOutputStream nbtOutputStream = new NBTOutputStream(new FileOutputStream(file));
			nbtOutputStream.writeTag(tag);
			nbtOutputStream.close();

			return true;
		}
		catch(final Exception e)
		{
			LOGGER.error("Error during NBT generation : {}", e);
		}

		return false;
	}

	public static boolean writeFile(final CompoundTag tag, final OutputStream out)
	{
		if(tag == null)
		{
			LOGGER.error("Can't write to file a null tag !");
			return false;
		}

		try
		{
			final NBTOutputStream nbtOutputStream = new NBTOutputStream(out);
			nbtOutputStream.writeTag(tag);
			nbtOutputStream.close();

			return true;
		}
		catch(final Exception e)
		{
			LOGGER.error("Error during NBT generation : {}", e);
		}

		return false;
	}

	public static CompoundTag readFile(final InputStream instr)
	{
		try
		{
			if(instr == null || instr.available() == 0)
			{
				return null;
			}
		}
		catch(final IOException e)
		{
		}

		try
		{
			final NBTInputStream nbtInputStream = new NBTInputStream(instr);
			final CompoundTag tag = (CompoundTag) nbtInputStream.readTag();
			nbtInputStream.close();

			return tag;
		}
		catch(final IOException e)
		{
			LOGGER.error("Error during NBT read : {}", e.getMessage());
		}

		return null;
	}

	public static CompoundTag readVFSFile(final InputStream instr)
	{
		if(instr == null)
		{
			return null;
		}

		try
		{
			final NBTInputStream nbtInputStream = new NBTInputStream(instr);
			final CompoundTag tag = (CompoundTag) nbtInputStream.readTag();
			nbtInputStream.close();

			return tag;
		}
		catch(final IOException e)
		{
			LOGGER.error("Error during NBT read : {}", e.getMessage());
		}

		return null;
	}

	public static CompoundTag readFile(final String fileName)
	{
		return readFile(new File(fileName));
	}

	public static CompoundTag readFile(final File file)
	{
		try
		{
			return readFile(new FileInputStream(file));
		}
		catch(final FileNotFoundException e)
		{
		}

		return null;
	}
}
