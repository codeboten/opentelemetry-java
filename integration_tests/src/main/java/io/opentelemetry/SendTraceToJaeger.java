/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.exporters.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;

public class SendTraceToJaeger {
  // Jaeger Endpoint URL and PORT
  private final String ip; // = "jaeger";
  private final int port; // = 14250;

  // OTel API
  private final Tracer tracer = OpenTelemetry.getTracer("io.opentelemetry.SendTraceToJaeger");

  public SendTraceToJaeger(String ip, int port) {
    this.ip = ip;
    this.port = port;
  }

  private void setupJaegerExporter() {
    // Create a channel towards Jaeger end point
    ManagedChannel jaegerChannel =
        ManagedChannelBuilder.forAddress(ip, port).usePlaintext().build();
    // Export traces to Jaeger
    JaegerGrpcSpanExporter jaegerExporter =
        JaegerGrpcSpanExporter.newBuilder()
            .setServiceName("integration test")
            .setChannel(jaegerChannel)
            .setDeadlineMs(30000)
            .build();

    // Set to process the spans by the Jaeger Exporter
    OpenTelemetrySdk.getTracerProvider()
        .addSpanProcessor(SimpleSpanProcessor.newBuilder(jaegerExporter).build());
  }

  private void myWonderfulUseCase() {
    // Generate a span
    Span span = this.tracer.spanBuilder("Start my wonderful use case").startSpan();
    span.addEvent("Event 0");
    // execute my use case - here we simulate a wait
    doWait();
    span.addEvent("Event 1");
    span.end();
  }

  private static void doWait() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // catch
    }
  }

  /**
   * Main method.
   *
   * @param args args
   */
  public static void main(String[] args) {
    // Parsing the input
    if (args.length < 2) {
      System.out.println("Missing [hostname] [port]");
      System.exit(1);
    }
    String ip = args[0];
    int port = Integer.parseInt(args[1]);

    // Start the example
    SendTraceToJaeger example = new SendTraceToJaeger(ip, port);
    example.setupJaegerExporter();
    example.myWonderfulUseCase();
    // wait some seconds
    doWait();
    System.out.println("Bye");
  }
}
