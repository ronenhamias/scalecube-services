package io.scalecube.services.transport.dispatchers;

import io.scalecube.services.ServiceMethodDefinition;
import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.codecs.api.ServiceMessageCodec;
import io.scalecube.services.transport.AbstractServiceMethodDispatcher;
import io.scalecube.services.transport.api.ServiceMethodDispatcher;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

public class MyRemoteDispatcher extends AbstractServiceMethodDispatcher<ServiceMessage>{

    @Override
    @SuppressWarnings("unchecked")
    public Publisher<ServiceMessage> requestChannel(final Publisher<ServiceMessage> request) {
        // FIXME: need to seek handler and invoke it.
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Publisher<ServiceMessage> requestStream(ServiceMessage request) {
        ServiceMethodDispatcher dispatcher = localServiceDispatchers.getDispatcher(request.qualifier());
        ServiceMessageCodec codec = null;

        return Flux.from(dispatcher.invoke(request)).map(resp -> codec.encodeData((ServiceMessage) resp));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Publisher<ServiceMessage> requestResponse(ServiceMessage request) {
        ServiceMethodDispatcher dispatcher = localServiceDispatchers.getDispatcher(request.qualifier());
        ServiceMessageCodec codec = null;

        return Mono.from(dispatcher.invoke(request)).map(resp -> codec.encodeData((ServiceMessage) resp));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Publisher<Void> fireAndForget(ServiceMessage request) {
        ServiceMethodDispatcher dispatcher = localServiceDispatchers.getDispatcher(request.qualifier());
        ServiceMessageCodec codec = null;

        return dispatcher.invoke(request);
    }

    private ServiceMessageCodec getCodec(ServiceMessage request) {
        return this.codecs.get("application/json");
    }
}
