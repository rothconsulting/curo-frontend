package ch.umb.curo.starter.plugin

import ch.umb.curo.starter.helper.camunda.CamundaVariableHelper
import ch.umb.curo.starter.helper.camunda.annotation.EnableInitCamundaVariables
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl
import org.camunda.bpm.engine.impl.util.xml.Element
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter

class VariableInitPlugin(
    val context: ConfigurableApplicationContext,
    val logger: Logger = LoggerFactory.getLogger(VariableInitPlugin::class.java)
) : AbstractProcessEnginePlugin() {
    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
        super.preInit(processEngineConfiguration)

        var preParseListeners = processEngineConfiguration.customPostBPMNParseListeners

        if (preParseListeners == null) {
            preParseListeners = ArrayList()
            processEngineConfiguration.customPostBPMNParseListeners = preParseListeners
        }

        val classes = getAllCamundaVariableClasses()
        if (classes.isEmpty()) {
            logger.debug("CURO: Skip variable initialization as there are no classes annotated with '@EnableInitCamundaVariables'")
            return
        }

        preParseListeners.add(object : AbstractBpmnParseListener() {
            override fun parseStartEvent(
                startEventElement: Element,
                scope: ScopeImpl,
                startEventActivity: ActivityImpl
            ) {
                super.parseStartEvent(startEventElement, scope, startEventActivity)
                val key = (startEventActivity.processDefinition as ProcessDefinitionEntity).key

                if (checkForProperty(startEventElement, "initializeVariables", "false")) {
                    logger.debug("CURO: Skip variable initialization for '$key'")
                    return
                }

                val possibleClasses = classes.filter {
                    val annotationValue =
                        Class.forName(it.beanClassName).getAnnotation(EnableInitCamundaVariables::class.java).value
                    annotationValue.isEmpty() || annotationValue.contains(key)
                }

                if (possibleClasses.isNotEmpty()) {
                    logger.info(
                        "CURO: Attach generic start listener to '$key' for variables initialization on process start. (related variable classes: ${
                            possibleClasses.joinToString(
                                ", "
                            ) { it.beanClassName!! }
                        })"
                    )
                    attachInitListener(possibleClasses, startEventActivity)
                }
            }
        })
    }

    private fun checkForProperty(element: Element, name: String, value: String? = null): Boolean {
        val extensionElement: Element = element.element("extensionElements") ?: return false
        val propertiesElement = extensionElement.element("properties") ?: return false
        val propertyList = propertiesElement.elements("property") ?: return false

        return propertyList.any { property ->
            val propertyName = property.attribute("name")
            val propertyValue = property.attribute("value")

            (propertyName == name && value == null) || (propertyName == name && propertyValue == value)
        }
    }

    private fun attachInitListener(classes: List<BeanDefinition>, startEventActivity: ActivityImpl) {
        startEventActivity.addListener(
            ExecutionListener.EVENTNAME_START
        ) { baseDelegateExecution ->
            classes.forEach {
                val clazz = Class.forName(it.beanClassName)
                logger.debug("CURO: Executing generic start listener to initialize variables of '${it.beanClassName}'")
                val constructor = clazz.getDeclaredConstructor()
                constructor.isAccessible = true
                CamundaVariableHelper.initVariables(constructor.newInstance(), baseDelegateExecution)
            }
        }
    }

    private fun getAllCamundaVariableClasses(): ArrayList<BeanDefinition> {
        val scanner = ClassPathScanningCandidateComponentProvider(false)
        scanner.addIncludeFilter(AnnotationTypeFilter(EnableInitCamundaVariables::class.java))

        val candidates = context.getBeansWithAnnotation(
            SpringBootApplication::class.java
        )
        return if (candidates.isNotEmpty()) {
            val basePackage = candidates.entries.first().value::class.java.packageName
            scanner.findCandidateComponents(basePackage).toCollection(arrayListOf())
        } else {
            arrayListOf()
        }
    }
}
