package org.nrvz.services.osgi.locator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * {@link BundleActivator} implementation that facilitates the lookup of services through
 * the running OSGi framework context.
 * 
 * <p style="font-style:font-face:Helvetica;italic;font-variant:small-caps;font-size:80%">
 * Copyright&copy; 2013 All rights reserved.<br>
 * Creation Date: 14 November 2013
 * </p>
 * 
 * @author Juan Narvaez [JN015941]
 */
public class OSGiServiceLocator implements BundleActivator
{
	/**
	 * static reference to this activator.
	 */
	private static volatile OSGiServiceLocator instance;
	
	/**
	 * {@link BundleContext} injected from the {@link #start(BundleContext)} method.
	 */
	private BundleContext context;
	
	@Override
	public void start(final BundleContext context) throws Exception
	{
		this.context = context;
		instance = this;
		
		//Automatically start Equinox DS bundles if available
		for (final Bundle bundle : context.getBundles())
		{
			if (bundle.getSymbolicName().equals("org.eclipse.equinox.ds") || 
					bundle.getSymbolicName().equals("org.eclipse.equinox.common"))
			{
				if (bundle.getState() != Bundle.ACTIVE && bundle.getState() != Bundle.STARTING)
				{
					try
					{
						bundle.start();
					}
					catch (BundleException e)
					{
						throw new IllegalArgumentException(e);
					}
				}
			}
		}
	}

	@Override
	public void stop(final BundleContext context) throws Exception
	{
		this.context = null;
		instance = null;
	}

	/**
	 * Retrieve the running {@link BundleContext}
	 * 
	 * @return {@link BundleContext}
	 */
	private BundleContext getBundleContext()
	{
		return context;
	}
	
	/**
	 * Retrieve the static instance
	 * 
	 * @return {@link OSGiServiceLocator} instance
	 */
	private static OSGiServiceLocator getInstance()
	{
		return instance;
	}
	
	/**
	 * Retrieve a service by {@link Class} type
	 * 
	 * @param clazz {@link Class} type
	 * @return discovered service, or <code>null</code>
	 */
	public static <S> S getService(final Class<S> clazz)
	{
		final BundleContext context = getInstance().getBundleContext();
		
		if (context != null)
		{
			final ServiceReference<S> ref = context.getServiceReference(clazz);
			
			if (ref != null)
			{
				return context.getService(ref);
			}
		}
		
		return null;
	}
	
	/**
	 * Retrieves the first service found that matches the provided {@link Class} and filter.
	 * 
	 * @param clazz {@link Class} of the service to lookup
	 * @param filter {@link String} LDAP style filter to apply for search
	 * @return instance that was discovered, or <code>null</code>
	 */
	public static <S> S getService(final Class<S> clazz, final String filter)
	{
		final Collection<S> services = getServices(clazz, filter);
		
		if (!services.isEmpty())
		{
			return services.iterator().next();
		}
		
		return null;
	}
	
	/**
	 * Retrieve all service instances of the provided {@link Class} and filter.
	 * 
	 * @param clazz {@link Class} of the service to lookup
	 * @param filter {@link String} LDAP style filter to apply for search
	 * @return {@link Collection} of the discovered services, might be <code>empty</code>
	 */
	public static <S> Collection<S> getServices(final Class<S> clazz, final String filter)
	{
		final Collection<S> services = new ArrayList<S>();
		final BundleContext context = getInstance().getBundleContext();
		
		if (context != null)
		{
			try
			{
				final Collection<ServiceReference<S>> refs = context.getServiceReferences(clazz, filter);
				
				if (refs != null)
				{
					for (final ServiceReference<S> ref : refs)
					{
						final S service = context.getService(ref);
						
						if (service != null)
						{
							services.add(service);
						}
					}
				}
			}
			catch (final InvalidSyntaxException e)
			{
				throw new IllegalArgumentException(e);
			}
		}
		
		return services;
	}
	
	/**
	 * Register a service with the running OSGi framework.
	 * 
	 * @param clazz service {@link Class}
	 * @param service instance of the service
	 * @param properties any properties that identify the service
	 * @return {@link ServiceRegistration} reference
	 */
	public static <S> ServiceRegistration<S> registerService(final Class<S> clazz, final S service, final Dictionary<String, String> properties)
	{
		final BundleContext context = getInstance().getBundleContext();
		
		if (context != null)
		{
			return context.registerService(clazz, service, properties);
		}
		
		return null;
	}
}
