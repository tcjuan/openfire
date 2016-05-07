package com.reucon.openfire.plugin.archive.xep0313;

import java.util.ArrayList;


import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import com.reucon.openfire.plugin.archive.xep.AbstractXepSupport;
import org.jivesoftware.openfire.disco.ServerFeaturesProvider;
import org.jivesoftware.openfire.disco.IQDiscoInfoHandler;
import org.jivesoftware.util.Log;

import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;

import java.util.*;

import com.reucon.openfire.plugin.archive.xep.AbstractIQHandler;
import org.jivesoftware.openfire.plugin.MonitoringPlugin;
/**
 * Encapsulates support for <a
 * href="http://www.xmpp.org/extensions/xep-0313.html">XEP-0313</a>.
 */
public class Xep0313Support  {

	private static final String NAMESPACE = "urn:xmpp:mam:0";
    final XMPPServer server;
    final Map<String, IQHandler> element2Handlers;
    final IQHandler iqDispatcher;
    final Collection<IQHandler> iqHandlers;

	public Xep0313Support(XMPPServer server) {
	
		//super(server, NAMESPACE,NAMESPACE, "XEP-0313 IQ Dispatcher");
		
		 this.server = server;
	        this.element2Handlers = Collections.synchronizedMap(new HashMap<String, IQHandler>());
	        this.iqDispatcher = new AbstractIQHandler("XEP-0313 IQ Dispatcher", null, NAMESPACE) {
	            public IQ handleIQ(IQ packet) throws UnauthorizedException
	            {
	              //  if (!MonitoringPlugin.getInstance().isEnabled())
	              //  {
	              //      return error(packet, PacketError.Condition.conflict );
	              //  }

	                final IQHandler iqHandler = element2Handlers.get("XEP-0313");
	                if (iqHandler != null)
	                {
	                    return iqHandler.handleIQ(packet);
	                }
	                else
	                {
	                    return error(packet, PacketError.Condition.conflict );
	                }
	            }
	        };
	        
	        iqHandlers = new ArrayList<IQHandler>();

	       
	
		iqHandlers.add(new IQQueryHandler());
	}


	public void start()
	{
    for (IQHandler iqHandler : iqHandlers)
    {
        try
        {
            iqHandler.initialize(server);
            iqHandler.start();
        }
        catch (Exception e)
        {
            Log.error("Unable to initialize and start " + iqHandler.getClass());
            continue;
        }

        element2Handlers.put("XEP-0313", iqHandler);
        if (iqHandler instanceof ServerFeaturesProvider)
        {
            for (Iterator<String> i = ((ServerFeaturesProvider) iqHandler).getFeatures(); i.hasNext(); )
            {
            	Log.info("Start XEP-0313 IQHandler and add feature! ");
                server.getIQDiscoInfoHandler().addServerFeature(i.next());
            }
        }
    }
    server.getIQDiscoInfoHandler().addServerFeature(NAMESPACE);
    server.getIQRouter().addHandler(iqDispatcher);
	}

	public void stop()
	{
    IQRouter iqRouter = server.getIQRouter();
    IQDiscoInfoHandler iqDiscoInfoHandler = server.getIQDiscoInfoHandler();

    for (IQHandler iqHandler : iqHandlers)
    {
        element2Handlers.remove(iqHandler.getInfo().getName());
        try
        {
            iqHandler.stop();
            iqHandler.destroy();
        }
        catch (Exception e)
        {
            Log.warn("Unable to stop and destroy " + iqHandler.getClass());
        }

        if (iqHandler instanceof ServerFeaturesProvider)
        {
            for (Iterator<String> i = ((ServerFeaturesProvider) iqHandler).getFeatures(); i.hasNext(); )
            {
                if (iqDiscoInfoHandler != null)
                {
                    iqDiscoInfoHandler.removeServerFeature(i.next());
                }
            }
        }
    }
    if (iqRouter != null)
    {
        iqRouter.removeHandler(iqDispatcher);
    }
	}
}