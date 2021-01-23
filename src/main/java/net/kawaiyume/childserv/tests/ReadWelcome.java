/* vim: set filetype=java: ts=4: sw=4: */
/*
  Copyright 2021
  
  Licensed by Thales TCS
  ----------------------------------------------------------------------------
  Created 23/01/2021
  ----------------------------------------------------------------------------
  ReadWelcome.java
  ----------------------------------------------------------------------------
  <TAB> = 4 <space>
  UTF-8
  ----------------------------------------------------------------------------
 */

package net.kawaiyume.childserv.tests;

import net.kawaiyume.childserv.WelcomeRoom;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;

/**
 * @author Nicolas VEYSSIERE (T0047283 nicolas.veyssiere@thalesgroup.com)
 */
public class ReadWelcome
{
    public static void main(final String[] args)
    {
        try
        {
            final String message = String.join("\n", Files.readAllLines(new File("welcome.md").toPath(), StandardCharsets.UTF_8));

            System.err.println(message);
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }
}
