package dev.runtime_lab.flowit.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import dev.runtime_lab.flowit.global.stereotype.InternalService;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
	packages = "dev.runtime_lab.flowit",
	importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

	@ArchTest
	static final ArchRule controllers_must_not_depend_on_internal_services = noClasses()
		.that().resideInAPackage("..controller..")
		.should().dependOnClassesThat().areAnnotatedWith(InternalService.class);

	@ArchTest
	static final ArchRule controllers_must_not_depend_on_internal_service_packages = noClasses()
		.that().resideInAPackage("..controller..")
		.should().dependOnClassesThat().resideInAPackage("..service.internal..");
}
