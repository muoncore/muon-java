package io.muoncore;

import io.muoncore.channel.Channels;
import io.muoncore.channel.support.Scheduler;
import io.muoncore.codec.Codecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.descriptors.SchemasDescriptor;
import io.muoncore.descriptors.ServiceExtendedDescriptor;
import io.muoncore.descriptors.ServiceExtendedDescriptorSource;
import io.muoncore.protocol.DynamicRegistrationServerStacks;
import io.muoncore.protocol.ServerRegistrar;
import io.muoncore.protocol.ServerStacks;
import io.muoncore.protocol.defaultproto.DefaultServerProtocol;
import io.muoncore.protocol.introspection.SchemaIntrospectionRequest;
import io.muoncore.protocol.introspection.server.IntrospectionServerProtocolStack;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.TransportControl;
import io.muoncore.transport.client.MultiTransportClient;
import io.muoncore.transport.client.SimpleTransportMessageDispatcher;
import io.muoncore.transport.client.TransportClient;
import io.muoncore.transport.client.TransportMessageDispatcher;
import io.muoncore.transport.sharedsocket.server.SharedChannelServerStacks;
import lombok.extern.slf4j.Slf4j;
import reactor.Environment;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple bundle of default Muon protocol stacks
 */
@Slf4j
public class MultiTransportMuon implements Muon, ServerRegistrarSource {

  private MultiTransportClient transportClient;
  private TransportControl transportControl;
  private Discovery discovery;
  private ServerStacks protocols;
  private ServerRegistrar registrar;
  private Codecs codecs;
  private AutoConfiguration configuration;
  private Scheduler scheduler;


  private UUID localInstanceId = UUID.randomUUID();

  public MultiTransportMuon(
    AutoConfiguration configuration,
    Discovery discovery,
    List<MuonTransport> transports, Codecs codecs) {
    Environment.initializeIfEmpty();
    this.configuration = configuration;
    this.codecs = codecs;
    TransportMessageDispatcher wiretap = new SimpleTransportMessageDispatcher();
    MultiTransportClient client = new MultiTransportClient(
      transports, wiretap, configuration, discovery, codecs);
    this.transportClient = client;
    this.transportControl = client;
    this.discovery = discovery;
    this.scheduler = new Scheduler();

    DynamicRegistrationServerStacks stacks = new DynamicRegistrationServerStacks(
      new DefaultServerProtocol(codecs, configuration, discovery),
      wiretap);
    this.protocols = new SharedChannelServerStacks(stacks, codecs);
    this.registrar = stacks;

    initServerStacks(stacks);

    transports.forEach(tr -> tr.start(discovery, this.protocols, codecs, getScheduler()));

    discovery.advertiseLocalService(
      new InstanceDescriptor(
        localInstanceId.toString(),
        configuration.getServiceName(),
        configuration.getTags(),
        Arrays.asList(codecs.getAvailableCodecs()),
        transports.stream().map(MuonTransport::getLocalConnectionURI)
          .collect(Collectors.toList()),
        generateCapabilities()));

    discovery.blockUntilReady();
  }

  private Set<String> generateCapabilities() {
    Set<String> capabilities = new HashSet<>();
    capabilities.add(SharedChannelServerStacks.PROTOCOL);
    return capabilities;
  }

  @Override
  public ServerRegistrar getProtocolStacks() {
    return registrar;
  }

  private void initServerStacks(DynamicRegistrationServerStacks stacks) {
    stacks.registerServerProtocol(new IntrospectionServerProtocolStack(
      new ServiceExtendedDescriptorSource() {
        @Override
        public ServiceExtendedDescriptor getServiceExtendedDescriptor() {
          return new ServiceExtendedDescriptor(configuration.getServiceName(), registrar.getProtocolDescriptors());
        }

        @Override
        public SchemasDescriptor getSchemasDescriptor(SchemaIntrospectionRequest request) {
          return registrar.getSchemasDescriptor(request.getProtocol(), request.getEndpoint());
        }
      }, codecs, discovery));
  }

  @Override
  public Codecs getCodecs() {
    return codecs;
  }

  @Override
  public Discovery getDiscovery() {
    return discovery;
  }

  @Override
  public TransportClient getTransportClient() {
    return transportClient;
  }

  @Override
  public AutoConfiguration getConfiguration() {
    return configuration;
  }

  @Override
  public void shutdown() {
    discovery.shutdown();
    transportClient.shutdown();
    Channels.shutdown();
  }

  @Override
  public TransportControl getTransportControl() {
    return transportControl;
  }

  public Scheduler getScheduler() {
    return scheduler;
  }
}
