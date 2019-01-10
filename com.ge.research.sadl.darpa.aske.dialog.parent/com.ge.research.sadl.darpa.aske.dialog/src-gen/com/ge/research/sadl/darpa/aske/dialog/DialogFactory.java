/**
 * generated by Xtext 2.14.0.RC1
 */
package com.ge.research.sadl.darpa.aske.dialog;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see com.ge.research.sadl.darpa.aske.dialog.DialogPackage
 * @generated
 */
public interface DialogFactory extends EFactory
{
  /**
   * The singleton instance of the factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  DialogFactory eINSTANCE = com.ge.research.sadl.darpa.aske.dialog.impl.DialogFactoryImpl.init();

  /**
   * Returns a new object of class '<em>Response Statement</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Response Statement</em>'.
   * @generated
   */
  ResponseStatement createResponseStatement();

  /**
   * Returns a new object of class '<em>String Response</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>String Response</em>'.
   * @generated
   */
  StringResponse createStringResponse();

  /**
   * Returns a new object of class '<em>What Statement</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>What Statement</em>'.
   * @generated
   */
  WhatStatement createWhatStatement();

  /**
   * Returns a new object of class '<em>What Is Statement</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>What Is Statement</em>'.
   * @generated
   */
  WhatIsStatement createWhatIsStatement();

  /**
   * Returns a new object of class '<em>What Values Statement</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>What Values Statement</em>'.
   * @generated
   */
  WhatValuesStatement createWhatValuesStatement();

  /**
   * Returns a new object of class '<em>How Many Values Statement</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>How Many Values Statement</em>'.
   * @generated
   */
  HowManyValuesStatement createHowManyValuesStatement();

  /**
   * Returns a new object of class '<em>Modified Ask Statement</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Modified Ask Statement</em>'.
   * @generated
   */
  ModifiedAskStatement createModifiedAskStatement();

  /**
   * Returns the package supported by this factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the package supported by this factory.
   * @generated
   */
  DialogPackage getDialogPackage();

} //DialogFactory
