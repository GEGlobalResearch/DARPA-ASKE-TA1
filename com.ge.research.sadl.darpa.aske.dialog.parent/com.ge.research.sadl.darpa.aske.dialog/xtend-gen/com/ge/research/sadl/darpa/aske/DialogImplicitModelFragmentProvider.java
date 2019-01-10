/**
 * © 2014-2017 General Electric Company – All Rights Reserved
 * 
 * This software and any accompanying data and documentation are CONFIDENTIAL
 * INFORMATION of the General Electric Company (“GE�?) and may contain trade secrets
 * and other proprietary information.  It is intended for use solely by GE and authorized
 * personnel.
 */
package com.ge.research.sadl.darpa.aske;

import com.ge.research.sadl.processing.ISadlImplicitModelFragmentProvider;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Functions.Function0;

/**
 * Implicit model fragment provider for SRL and SADL. This service is used for
 * appending additional content to the implicit SADL model.
 * 
 * <p>
 * This class is registered as an Eclipse-based extension point and as an SPI to support both
 * the headless and the Eclipse platform service discovery.
 */
@SuppressWarnings("all")
public class DialogImplicitModelFragmentProvider implements ISadlImplicitModelFragmentProvider {
  public static final String DIALOG_IMPLICIT_MODEL_FRAGMENT = new Function0<String>() {
    public String apply() {
      StringConcatenation _builder = new StringConcatenation();
      return _builder.toString();
    }
  }.apply();
  
  @Override
  public String getFragmentToAppend() {
    return DialogImplicitModelFragmentProvider.DIALOG_IMPLICIT_MODEL_FRAGMENT;
  }
}
