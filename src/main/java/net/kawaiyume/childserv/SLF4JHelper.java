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
  26 october 2020
  ----------------------------------------------------------------------------
  SLF4JHelper.java
  ----------------------------------------------------------------------------
  <TAB> = 4 <space>
  ----------------------------------------------------------------------------
 */

package net.kawaiyume.childserv;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * @author iXô (ixo@kawaiyume.net)
 */
public final class SLF4JHelper
{
    private SLF4JHelper()
    {
    }

    public static void init()
    {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try
        {

            final JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);

            ch.qos.logback.classic.Logger rootLogger = context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
            rootLogger.detachAppender("console");

            configurator.doConfigure(buildELKInputStream(), UUID.randomUUID().toString());
        }
        catch (JoranException je)
        {
            // StatusPrinter will handle this
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    public static String logger(final String logger, final String level)
    {
        return "<logger name=\"" + logger + "\" level=\"" + level + "\" />";
    }

    private static InputStream buildELKInputStream()
    {
        final StringBuilder elkCfg = new StringBuilder();

        elkCfg.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        elkCfg.append("<configuration debug=\"false\">");

        elkCfg.append("     <appender name=\"STDOUT\" class=\"ch.qos.logback.core.ConsoleAppender\">");
        elkCfg.append("         <encoder>");
        elkCfg.append("             <pattern>%d{HH:mm:ss.SSS} %-4relative [%thread] %logger %-5level %class - %msg%n</pattern>");
        //elkCfg.append("             <pattern>%d{HH:mm:ss.SSS} %-4relative [%thread] %-5level %class - %msg%n</pattern>");
        elkCfg.append("         </encoder>");
        elkCfg.append("     </appender>");

        elkCfg.append(SLF4JHelper.logger("org.jboss.resteasy.plugins.providers.DocumentProvider", "WARN"));
        elkCfg.append(SLF4JHelper.logger("org.jboss.resteasy.plugins.providers.jaxb.AbstractJAXBProvider", "WARN"));
        elkCfg.append(SLF4JHelper.logger("io.github.ma1uta.matrix.client.filter.LoggingFilter", "WARN"));
        elkCfg.append(SLF4JHelper.logger("org.jboss.resteasy.resteasy_jaxrs.i18n", "WARN"));
        elkCfg.append(SLF4JHelper.logger("org.jboss.resteasy.plugins.providers.jaxb.i18n", "WARN"));
        elkCfg.append(SLF4JHelper.logger("org.jboss.resteasy.core.interception.jaxrs.AbstractWriterInterceptorContext", "WARN"));
        elkCfg.append(SLF4JHelper.logger("org.jboss.resteasy.microprofile.client.ProxyInvocationHandler", "ERROR"));
        elkCfg.append(SLF4JHelper.logger("io.github.ma1uta.matrix.client.methods.blocked.AuthMethods", "WARN"));

        final String rootLevel = "DEBUG";

        elkCfg.append("     <root level=\"").append(rootLevel).append("\">");
        elkCfg.append("         <appender-ref ref=\"STDOUT\" />");
        elkCfg.append("     </root>");
        elkCfg.append("</configuration>");

        return new ByteArrayInputStream(elkCfg.toString().getBytes(StandardCharsets.UTF_8));
    }
}
