package com.ezamora.coffeeshop.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/** Test de arquitectura: protege las invariantes hexagonales (dependencias hacia adentro). */
@AnalyzeClasses(packages = "com.ezamora.coffeeshop", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String BASE = "com.ezamora.coffeeshop";

    @ArchTest
    static final ArchRule domain_is_framework_free = noClasses().that().resideInAPackage(BASE + ".domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta..", "lombok..", "org.slf4j..");

    /** INV-12: en application solo se permiten @Service y @Transactional de Spring. */
    @ArchTest
    static final ArchRule application_only_allows_spring_transactional = noClasses().that()
            .resideInAPackage(BASE + ".application..")
            .should().dependOnClassesThat(
                    JavaClass.Predicates.resideInAnyPackage("org.springframework..")
                            .and(DescribedPredicate.not(JavaClass.Predicates.equivalentTo(Transactional.class)))
                            // tipos de los miembros de @Transactional (dependencia mínima, no son lógica de framework)
                            .and(DescribedPredicate.not(JavaClass.Predicates.equivalentTo(Isolation.class)))
                            .and(DescribedPredicate.not(JavaClass.Predicates.equivalentTo(Propagation.class)))
                            .and(DescribedPredicate.not(JavaClass.Predicates.equivalentTo(Service.class))))
            .because("solo @Service y @Transactional están permitidas en application");

    @ArchTest
    static final ArchRule application_has_no_jakarta_lombok_or_slf4j = noClasses().that()
            .resideInAPackage(BASE + ".application..")
            .should().dependOnClassesThat().resideInAnyPackage("jakarta..", "lombok..", "org.slf4j..");

    /** INV-18: solo el flujo que escribe en más de un puerto de salida define su unidad de trabajo. */
    @ArchTest
    static final ArchRule use_cases_have_no_class_level_transactional = noClasses().that()
            .resideInAPackage(BASE + ".application.service..")
            .should().beAnnotatedWith(Transactional.class);

    @ArchTest
    static final ArchRule pay_order_is_transactional = methods().that()
            .areDeclaredIn(BASE + ".application.service.CoffeeShop").and().haveName("payOrder")
            .should().beAnnotatedWith(Transactional.class);

    /**
     * Lista permitida de métodos transaccionales: los que escriben en más de un puerto.
     * payOrder guarda el pago (Payments) y la orden pagada (Orders) y no debe dejar estado parcial.
     */
    @ArchTest
    static final ArchRule only_multi_port_writers_are_transactional = methods().that()
            .areDeclaredInClassesThat().resideInAPackage(BASE + ".application.service..")
            .and().areAnnotatedWith(Transactional.class)
            .should().haveName("payOrder");

    @ArchTest
    static final ArchRule use_cases_are_services = classes().that()
            .resideInAPackage(BASE + ".application.service..").and().arePublic()
            .should().beAnnotatedWith(Service.class);

    @ArchTest
    static final ArchRule config_does_not_wire_use_cases = noClasses().that()
            .resideInAPackage(BASE + ".infrastructure.config..")
            .should(new ArchCondition<JavaClass>("declare @Bean methods returning use cases") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    item.getMethods().stream()
                            .filter(m -> m.isAnnotatedWith("org.springframework.context.annotation.Bean"))
                            .filter(m -> m.getRawReturnType().getPackageName().startsWith(BASE + ".application"))
                            .forEach(m -> events.add(SimpleConditionEvent.satisfied(item, m.getFullName())));
                }
            });

    @ArchTest
    static final ArchRule config_does_not_implement_input_ports = noClasses().that()
            .resideInAPackage(BASE + ".infrastructure.config..")
            .should().implement(JavaClass.Predicates.resideInAPackage(BASE + ".application.in.."));

    @ArchTest
    static final ArchRule domain_has_no_model_subpackage = noClasses().should()
            .resideInAPackage(BASE + ".domain.model..")
            .because("el dominio se organiza por agregado/concepto, sin nivel 'model'");

    @ArchTest
    static final ArchRule inner_layers_do_not_depend_on_infrastructure = noClasses().that()
            .resideInAnyPackage(BASE + ".domain..", BASE + ".application..")
            .should().dependOnClassesThat().resideInAPackage(BASE + ".infrastructure..");

    @ArchTest
    static final ArchRule domain_does_not_depend_on_application = noClasses().that().resideInAPackage(BASE + ".domain..")
            .should().dependOnClassesThat().resideInAPackage(BASE + ".application..");

    @ArchTest
    static final ArchRule adapters_in_do_not_depend_on_adapters_out = noClasses().that()
            .resideInAPackage(BASE + ".infrastructure.adapter.in..")
            .should().dependOnClassesThat().resideInAPackage(BASE + ".infrastructure.adapter.out..");

    private static final String PERSISTENCE = BASE + ".infrastructure.adapter.out.persistence";

    @ArchTest
    static final ArchRule adapters_out_do_not_depend_on_adapters_in = noClasses().that()
            .resideInAPackage(BASE + ".infrastructure.adapter.out..")
            .should().dependOnClassesThat().resideInAPackage(BASE + ".infrastructure.adapter.in..");

    @ArchTest
    static final ArchRule persistence_order_does_not_depend_on_payment = noClasses().that()
            .resideInAPackage(PERSISTENCE + ".order..")
            .should().dependOnClassesThat().resideInAPackage(PERSISTENCE + ".payment..");

    @ArchTest
    static final ArchRule persistence_payment_does_not_depend_on_order = noClasses().that()
            .resideInAPackage(PERSISTENCE + ".payment..")
            .should().dependOnClassesThat().resideInAPackage(PERSISTENCE + ".order..");

    @ArchTest
    static final ArchRule jpa_entities_are_named_with_JpaEntity_suffix = classes().that()
            .areAnnotatedWith("jakarta.persistence.Entity")
            .should().haveSimpleNameEndingWith("JpaEntity");

    @ArchTest
    static final ArchRule no_package_cycles = slices().matching(BASE + ".(*)..").should().beFreeOfCycles();
}
