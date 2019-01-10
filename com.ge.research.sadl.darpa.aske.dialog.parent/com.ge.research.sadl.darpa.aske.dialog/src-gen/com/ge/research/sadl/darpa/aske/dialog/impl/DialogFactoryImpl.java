/**
 * generated by Xtext 2.14.0.RC1
 */
package com.ge.research.sadl.darpa.aske.dialog.impl;

import com.ge.research.sadl.darpa.aske.dialog.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class DialogFactoryImpl extends EFactoryImpl implements DialogFactory
{
  /**
   * Creates the default factory implementation.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public static DialogFactory init()
  {
    try
    {
      DialogFactory theDialogFactory = (DialogFactory)EPackage.Registry.INSTANCE.getEFactory(DialogPackage.eNS_URI);
      if (theDialogFactory != null)
      {
        return theDialogFactory;
      }
    }
    catch (Exception exception)
    {
      EcorePlugin.INSTANCE.log(exception);
    }
    return new DialogFactoryImpl();
  }

  /**
   * Creates an instance of the factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public DialogFactoryImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public EObject create(EClass eClass)
  {
    switch (eClass.getClassifierID())
    {
      case DialogPackage.RESPONSE_STATEMENT: return createResponseStatement();
      case DialogPackage.STRING_RESPONSE: return createStringResponse();
      case DialogPackage.WHAT_STATEMENT: return createWhatStatement();
      case DialogPackage.WHAT_IS_STATEMENT: return createWhatIsStatement();
      case DialogPackage.WHAT_VALUES_STATEMENT: return createWhatValuesStatement();
      case DialogPackage.HOW_MANY_VALUES_STATEMENT: return createHowManyValuesStatement();
      case DialogPackage.MODIFIED_ASK_STATEMENT: return createModifiedAskStatement();
      default:
        throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
    }
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ResponseStatement createResponseStatement()
  {
    ResponseStatementImpl responseStatement = new ResponseStatementImpl();
    return responseStatement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public StringResponse createStringResponse()
  {
    StringResponseImpl stringResponse = new StringResponseImpl();
    return stringResponse;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public WhatStatement createWhatStatement()
  {
    WhatStatementImpl whatStatement = new WhatStatementImpl();
    return whatStatement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public WhatIsStatement createWhatIsStatement()
  {
    WhatIsStatementImpl whatIsStatement = new WhatIsStatementImpl();
    return whatIsStatement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public WhatValuesStatement createWhatValuesStatement()
  {
    WhatValuesStatementImpl whatValuesStatement = new WhatValuesStatementImpl();
    return whatValuesStatement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public HowManyValuesStatement createHowManyValuesStatement()
  {
    HowManyValuesStatementImpl howManyValuesStatement = new HowManyValuesStatementImpl();
    return howManyValuesStatement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ModifiedAskStatement createModifiedAskStatement()
  {
    ModifiedAskStatementImpl modifiedAskStatement = new ModifiedAskStatementImpl();
    return modifiedAskStatement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public DialogPackage getDialogPackage()
  {
    return (DialogPackage)getEPackage();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @deprecated
   * @generated
   */
  @Deprecated
  public static DialogPackage getPackage()
  {
    return DialogPackage.eINSTANCE;
  }

} //DialogFactoryImpl
