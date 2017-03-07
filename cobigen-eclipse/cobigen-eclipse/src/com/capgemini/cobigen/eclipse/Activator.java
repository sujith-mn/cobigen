package com.capgemini.cobigen.eclipse;

import java.util.UUID;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.capgemini.cobigen.eclipse.common.constants.InfrastructureConstants;
import com.capgemini.cobigen.eclipse.workbenchcontrol.ConfigurationProjectListener;
import com.capgemini.cobigen.impl.PluginRegistry;
import com.capgemini.cobigen.impl.TemplateEngineRegistry;
import com.capgemini.cobigen.javaplugin.JavaPluginActivator;
import com.capgemini.cobigen.jsonplugin.JSONPluginActivator;
import com.capgemini.cobigen.propertyplugin.PropertyMergerPluginActivator;
import com.capgemini.cobigen.senchaplugin.SenchaPluginActivator;
import com.capgemini.cobigen.tempeng.freemarker.FreeMarkerTemplateEngine;
import com.capgemini.cobigen.textmerger.TextMergerPluginActivator;
import com.capgemini.cobigen.xmlplugin.XmlPluginActivator;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "com.capgemini.cobigen.eclipseplugin"; //$NON-NLS-1$

    /**
     * The shared instance
     */
    private static Activator plugin;

    /** {@link IResourceChangeListener} for the configuration project */
    private IResourceChangeListener configurationProjectListener = new ConfigurationProjectListener();

    /**
     * Current state of the {@link IResourceChangeListener} for the configuration project
     */
    private volatile boolean configurationProjectListenerStarted = false;

    /**
     * Assigning logger to Activator
     */
    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    /**
     * The constructor
     */
    public Activator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        MDC.put(InfrastructureConstants.CORRELATION_ID, UUID.randomUUID().toString());
        super.start(context);
        plugin = this;
        PluginRegistry.loadPlugin(JavaPluginActivator.class);
        PluginRegistry.loadPlugin(XmlPluginActivator.class);
        PluginRegistry.loadPlugin(PropertyMergerPluginActivator.class);
        PluginRegistry.loadPlugin(TextMergerPluginActivator.class);
        PluginRegistry.loadPlugin(JSONPluginActivator.class);
        PluginRegistry.loadPlugin(SenchaPluginActivator.class);
        TemplateEngineRegistry.register(FreeMarkerTemplateEngine.class);
        startConfigurationProjectListener();
        MDC.remove(InfrastructureConstants.CORRELATION_ID);
    }

    /**
     * Starts the ResourceChangeListener
     */
    public void startConfigurationProjectListener() {
        LOG.info("Start configuration project listener");
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                MDC.put(InfrastructureConstants.CORRELATION_ID, UUID.randomUUID().toString());
                synchronized (configurationProjectListener) {
                    if (configurationProjectListenerStarted) {
                        return;
                    }
                    ResourcesPlugin.getWorkspace().addResourceChangeListener(configurationProjectListener,
                        IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.POST_BUILD
                            | IResourceChangeEvent.POST_CHANGE);
                    configurationProjectListenerStarted = true;
                    LOG.info("ResourceChangeListener for configuration project started.");
                }
                MDC.remove(InfrastructureConstants.CORRELATION_ID);
            }
        });
    }

    /**
     * Stops the ResourceChangeListener
     * @author mbrunnli (Jun 24, 2015)
     */
    public void stopConfigurationListener() {
        LOG.info("Stop configuration project listener");
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                MDC.put(InfrastructureConstants.CORRELATION_ID, UUID.randomUUID().toString());
                synchronized (configurationProjectListener) {
                    if (!configurationProjectListenerStarted) {
                        return;
                    }
                    ResourcesPlugin.getWorkspace().removeResourceChangeListener(configurationProjectListener);
                    configurationProjectListenerStarted = false;
                    LOG.info("ResourceChangeListener for configuration project stopped.");
                }
                MDC.remove(InfrastructureConstants.CORRELATION_ID);
            }
        });
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

}
