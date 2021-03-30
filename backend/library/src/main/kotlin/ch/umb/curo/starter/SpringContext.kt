package ch.umb.curo.starter

import org.springframework.beans.BeansException
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

/**
 * Provides Spring context to non spring context classes.
 *
 * @author itsmefox
 */
@Component
class SpringContext : ApplicationContextAware {
    @Throws(BeansException::class)
    override fun setApplicationContext(context: ApplicationContext) {
        applicationContext = context
    }

    companion object {
        var applicationContext: ApplicationContext? = null

        fun <T : Any?> getBean(beanClass: Class<T>): T? {
            return applicationContext?.getBean(beanClass)
        }

        fun <T : Any?> getBeans(beanClass: Class<T>): List<T?> {
            return try {
                applicationContext?.getBeansOfType(beanClass)?.values?.toCollection(arrayListOf()) ?: listOf()
            } catch (e: NoSuchBeanDefinitionException){
                arrayListOf()
            }
        }
    }
}
