package com.ge.research.sadl.darpa.aske.ide.editor.contentassist

import com.ge.research.sadl.darpa.aske.services.DialogGrammarAccess
import com.ge.research.sadl.ide.editor.contentassist.SadlIdeContentProposalProvider
import com.ge.research.sadl.sADL.SadlModel
import com.ge.research.sadl.sADL.SadlResource
import com.google.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.Assignment
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistEntry
import org.eclipse.xtext.ide.editor.contentassist.IIdeContentProposalAcceptor
import org.eclipse.xtext.ide.editor.contentassist.IdeContentProposalCreator
import org.eclipse.xtext.ide.editor.contentassist.IdeContentProposalPriorities
import org.eclipse.xtext.scoping.IScopeProvider

import static com.ge.research.sadl.model.OntConceptType.*
import static com.ge.research.sadl.sADL.SADLPackage.Literals.*

class DialogIdeContentProposalProvider extends SadlIdeContentProposalProvider {

	@Inject
	DialogGrammarAccess grammarAccess

	@Inject
	IScopeProvider scopeProvider

	@Inject
	IdeContentProposalCreator proposalCreator

	@Inject
	IdeContentProposalPriorities proposalPriorities

	override protected _createProposals(Assignment assignment, ContentAssistContext ctx,
		IIdeContentProposalAcceptor acceptor) {

		if(excludedNamespaces !== null) excludedNamespaces.clear
		if(typeRestrictions !== null) typeRestrictions.clear
		switch (assignment) {
			case grammarAccess.targetModelNameAccess.targetResourceAssignment_2: {
				ctx.completeImports(acceptor)
			}
			case grammarAccess.targetModelNameAccess.aliasAssignment_3_1: {
				ctx.completeAlias(acceptor)
			}
			case grammarAccess.newExpressionStatementAccess.newExprAssignment_1: {
				ctx.completeAddStatements(acceptor)
			}
			default: {
				super._createProposals(assignment, ctx, acceptor)
			}
		}
	}

	protected def completeAddStatements(ContentAssistContext context, IIdeContentProposalAcceptor acceptor) {
		val sadlModel = context.currentModel.sadlModel
		val builtInFilter = context.implicitModelFilter
		if (sadlModel !== null) {
			val scope = scopeProvider.getScope(sadlModel, SADL_RESOURCE__NAME);
			val resources = scope.allElements.filter[builtInFilter.apply(it)].filter[EObjectOrProxy.isClassOrProperty].
				toMap[EObjectURI]
			for (entry : resources.entrySet) {
				val resource = entry.value.EObjectOrProxy as SadlResource
				val name = declarationExtensions.getConcreteName(resource)
				if (!name.nullOrEmpty) {
					val proposal = proposalCreator.createProposal(name, context, [
						description = resource.eClass.name
						kind = ContentAssistEntry.KIND_REFERENCE
						source = resource
					]);
					acceptor.accept(proposal, proposalPriorities.getDefaultPriority(proposal))
				}
			}
		}
	}

	override boolean shouldFilterBuiltIns(EObject model) {
		if (model === null || model.eIsProxy || model.eResource === null) {
			return false;
		}
		// We cannot retrieve SADL preferences for Dialog model elements
		if (!model.eResource.URI.toString.endsWith('.sadl')) {
			val sadlXtextResource = model.eResource.resourceSet.resources.findFirst [
				URI.toString.endsWith('.sadl') && contents.head instanceof SadlModel
			]
			return super.shouldFilterBuiltIns(sadlXtextResource.contents.head)
		}
		return super.shouldFilterBuiltIns(model)
	}

	protected def boolean isClassOrProperty(EObject it) {
		if (eIsProxy) {
			return false
		}
		if (it instanceof SadlResource) {
			val type = declarationExtensions.getOntConceptType(it)
			return type === CLASS || type === CLASS_PROPERTY || type === RDF_PROPERTY
		}
		return false
	}

	protected def SadlModel sadlModel(EObject it) {
		if (it === null || eIsProxy || eResource === null) {
			return null
		}
		val model = eResource.contents.head
		if (model instanceof SadlModel) {
			return model
		}
		return null
	}

}
