package net.sf.sahi.ssl;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Sahi - Web Automation and Test Tool
 * <p/>
 * Copyright  2006  V Narayan Raman
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class SSLHelperTest {

  @Before
  public void setup() {
    Configuration.init();
  }

  @Test
  @Ignore("FIXME")
  public void xtestSSLCommand() {
    SSLHelper helper = new SSLHelper();
    assertEquals("keytool.exe -genkey -alias www.sahi.co.in -keypass pwd -storepass pwd -keyalg RSA -keystore filekarasta -dname \"CN=www.sahi.co.in, OU=Sahi, O=Sahi, L=Bangalore, S=Karnataka, C=IN\"", helper.getPrintableSSLCommand(helper.getSSLCommand("www.sahi.co.in", "filekarasta", "pwd", "keytool.exe")).trim());
  }

  @Test
  public void testTokenizer() {
    String s = "keytool.exe -dname \"CN=www.sahi.co.in, OU=Sahi\"";
    String[] commandTokens = Utils.getCommandTokens(s);
    for (int i = 0; i < commandTokens.length; i++) {
      System.out.println(commandTokens[i]);
    }
  }

}