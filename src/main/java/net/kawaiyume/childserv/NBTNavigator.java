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
  NBTNavigator.java
  ----------------------------------------------------------------------------
  <TAB> = 4 <space>
  ----------------------------------------------------------------------------
 */

package net.kawaiyume.childserv;

import org.jnbt.ByteTag;
import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
import org.jnbt.ListTag;
import org.jnbt.LongTag;
import org.jnbt.StringTag;
import org.jnbt.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author iXô (ixo@kawaiyume.net)
 */
public class NBTNavigator
{
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(NBTNavigator.class);

	private File        file;
	private CompoundTag root;

	public NBTNavigator()
	{
		file = null;
		root = new CompoundTag("root");
	}

	public NBTNavigator(final String nbtFile)
	{
		this(new File(nbtFile), true);
	}

	public NBTNavigator(final CompoundTag root)
	{
		this.file = null;
		this.root = root;

	}

	public NBTNavigator(final String nbtFile, final boolean createNotFound)
	{
		this(new File(nbtFile), createNotFound);
	}

	public NBTNavigator(final File nbtFile)
	{
		this(nbtFile, true);
	}

	public NBTNavigator(final File nbtFile, final boolean createNotFound)
	{
		this.file = nbtFile;
		root = NBTHelper.readFile(nbtFile);

		if(root == null && createNotFound)
		{
			root = new CompoundTag("root");
			save();
		}
	}

	public File getFile()
	{
		return file;
	}

	public CompoundTag getRoot()
	{
		return root;
	}

	public boolean save()
	{
		if(file != null)
		{
			// create folders if needed
			try
			{
				file.getAbsoluteFile().getParentFile().mkdirs();
			}
			catch(final Exception e)
			{
			}

			try
			{
				return NBTHelper.writeFile(root, file);
			}
			catch(final Exception e)
			{
				return false;
			}
		}
		else
		{
			return true;
		}
	}

	public Tag<?> findTag(final String path)
	{
		final String[] paths = path.split("/");

		CompoundTag t = root;
		if(paths.length > 1)
		{
			for(int cpt = 0; cpt < paths.length - 1; cpt++)
			{
				if(t != null)
				{
					t = (CompoundTag) t.getValue().get(paths[cpt]);
				}
				else
				{
					return null;
				}
			}
		}

		if(t != null)
		{
			return t.getValue().get(paths[paths.length - 1]);
		}

		return null;
	}

	public Tag<?> findParent(final String path)
	{
		if(!path.contains("/"))
		{
			// no parent path, returning root
			return root;
		}

		// TODO
		return null;
	}

	public Long getLong(final String path)
	{
		final LongTag lTag = (LongTag) findTag(path);
		if(lTag == null)
		{
			return null;
		}

		return lTag.getValue();
	}

	public Integer getInteger(final String path)
	{
		final Tag<?> t = findTag(path);
		if(t instanceof IntTag)
		{
			return ((IntTag) t).getValue();
		}
		else if(t instanceof LongTag)
		{
			return ((LongTag) t).getValue().intValue();
		}

		return null;
	}

	public Boolean getBoolean(final String path)
	{
		final ByteTag bTag = (ByteTag) findTag(path);
		if(bTag == null)
		{
			return null;
		}

		return bTag.getValue() == 1;
	}

	public String getString(final String path)
	{
		final StringTag strTag = (StringTag) findTag(path);
		if(strTag == null)
		{
			return null;
		}

		return strTag.getValue();
	}

	public List<Tag<?>> getArrayCompounds(final String path)
	{
		final ListTag objectArrayTag = (ListTag) findTag(path);
		return objectArrayTag.getValue().stream().map(tag -> (CompoundTag) tag).collect(Collectors.toList());
	}

	public boolean isStringPresent(final String path)
	{
		final String value = getString(path);

		return value != null && !value.isEmpty();
	}

	public CompoundTag getCompound(final String path)
	{
		return (CompoundTag) findTag(path);
	}

	public void addCompound(final String path, final CompoundTag newTag)
	{
		final CompoundTag parent = (CompoundTag) findParent(path);
		parent.getValue().put(extractLeafPath(path), newTag);
	}

	public void addString(final String path, final String value)
	{
		addString(path, value, null);
	}

	public void addString(final String path, final String value, final String defaultValue)
	{
		final CompoundTag parent = (CompoundTag) findParent(path);
		if(parent == null)
		{
			LOGGER.error("Unable to append string tag to parent, as parent is not found");
			return;
		}

		final String what = extractLeafPath(path);

		if(value != null)
		{
			parent.getValue().put(what, new StringTag(what, value));
		}
		else if(defaultValue != null)
		{
			parent.getValue().put(what, new StringTag(what, defaultValue));
		}
	}

	public void addInteger(final String path, final int value)
	{
		final CompoundTag parent = (CompoundTag) findParent(path);

		final String what = extractLeafPath(path);
		parent.getValue().put(what, new IntTag(what, value));
	}

	public void addLong(final String path, final long value)
	{
		final CompoundTag parent = (CompoundTag) findParent(path);

		final String what = extractLeafPath(path);
		parent.getValue().put(what, new LongTag(what, value));
	}

	public long incLong(final String path, final int incAmount)
	{
		LongTag lTag = (LongTag) findTag(path);
		if(lTag == null)
		{
			final String what = extractLeafPath(path);
			lTag = new LongTag(what, incAmount);

			final CompoundTag parent = (CompoundTag) findParent(path);
			parent.getValue().put(what, lTag);
		}
		else
		{
			lTag.setValue(lTag.getValue() + incAmount);
		}

		return lTag.getValue();
	}

	public void addBoolean(final String path, final boolean bool)
	{
		final CompoundTag parent = (CompoundTag) findParent(path);

		final String what = extractLeafPath(path);
		parent.getValue().put(what, new ByteTag(what, bool ? (byte) 1 : (byte) 0));
	}

	public void del(final String path)
	{
		final CompoundTag parent = (CompoundTag) findParent(path);
		if(parent == null)
		{
			LOGGER.error("Unable to append string tag to parent, as parent is not found");
			return;
		}


		final String what = extractLeafPath(path);
		parent.getValue().remove(what);
	}
	
	public void addCompound(final CompoundTag tag)
	{
		addCompound(tag.getName(), tag);
	}

	protected static String extractLeafPath(final String path)
	{
		if(!path.contains("/"))
		{
			return path;
		}

		return path.substring(path.lastIndexOf("/") + 1);
	}
}
