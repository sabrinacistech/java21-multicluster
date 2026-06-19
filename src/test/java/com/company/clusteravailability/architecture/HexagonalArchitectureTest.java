package com.company.clusteravailability.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(packages = "com.company.clusteravailability", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domainDoesNotDependOnSpringOrJpa = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..");

    @ArchTest
    static final ArchRule applicationDoesNotDependOnInfrastructure = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule controllersLiveInRestAdapter = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage("..infrastructure.adapter.in.rest..");

    @ArchTest
    static final ArchRule jpaRepositoriesLiveInPersistenceAdapter = classes()
            .that().areAssignableTo(org.springframework.data.repository.Repository.class)
            .should().resideInAPackage("..infrastructure.adapter.out.persistence..");

    @ArchTest
    static final ArchRule jpaEntitiesLiveInPersistenceAdapter = classes()
            .that().areAnnotatedWith(jakarta.persistence.Entity.class)
            .should().resideInAPackage("..infrastructure.adapter.out.persistence..");

    @ArchTest
    static final ArchRule controllersDoNotDependOnJpaRepositories = noClasses()
            .that().resideInAPackage("..infrastructure.adapter.in.rest..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure.adapter.out.persistence..");

    @ArchTest
    static final ArchRule adaptersImplementPorts = classes()
            .that().haveSimpleNameEndingWith("Adapter")
            .should().implement(com.company.clusteravailability.domain.port.ClusterStatusRepositoryPort.class);
}
