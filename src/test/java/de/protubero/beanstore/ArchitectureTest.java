package de.protubero.beanstore;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.plantuml.PlantUmlArchCondition.adhereToPlantUmlDiagram;
import static com.tngtech.archunit.library.plantuml.PlantUmlArchCondition.Configurations.consideringOnlyDependenciesInDiagram;

import java.net.URL;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "de.protubero.beanstore", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {
	
	
	private static final URL plantUmlDiagram = ArchitectureTest.class.getResource("/lib_layers.uml");

	@ArchTest
	static final ArchRule code_should_adhere_to_layer_model =
			classes().should(adhereToPlantUmlDiagram(plantUmlDiagram, consideringOnlyDependenciesInDiagram()));

   
}