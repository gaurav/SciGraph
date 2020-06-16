/**
 * Copyright (C) 2014 The SciGraph authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.scigraph.annotation;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.apache.lucene.queryparser.classic.QueryParserBase;

public class EntityProcessorImplIncorrectIndexesTest {

  EntityProcessorImpl processor;
  EntityFormatConfiguration config = mock(EntityFormatConfiguration.class);

  // Example text extracted from PMC7111660.
  final static String text = "A novel L-type lectin was required for the multiplication of WSSV in red swamp crayfish (Procambarus clakii). Fig.\\u00a02: Tissues distribution of PcL-lectin. (A) The expressions of PcL-lectin mRNA in the six tissues of crayfish. The extracted total mRNAs from hemocytes, heart, hepatopancreas, gills, stomach and intestine were subjected to qRT-PCR assay. The 18S rRNA was used as reference. (B) The expression of PcL-lectin protein in the six tissues of crayfish. The extracted total proteins from the six tissues were subjected to Western blot assay. \u03b2-actin was used as internal control. The experiments were repeated three times. Different letters represented significant changes between groups, p < 0.05. The tissue distributions and expression profiles of PcL-lectin were analyzed using qRT-PCR with the PcL-lectin specific primers RT-PcL-lecF and RT-PcL-lecR (Table 1). The 18S rRNA was used as the reference and was amplified with the primers 18S F and 18SR (Table 1). The qRT-PCR was programmed at 95 \u00b0C for 10 min, followed by 40 cycles at 95 \u00b0C for 10 s, 60 \u00b0C for 1 min. DNA melting analysis was performed to confirm the specificity of the amplified products. The obtained data were statistically analyzed and calculated using the threshold cycle method as previously described [16]. Lectins are group of molecules which are highly specific for the binding of carbohydrate. It was earliest discovered in leguminous plants, therefore it was named legume lectin (or L-type lectin). Thereafter, many other members of lectins have been identified in most kinds of organisms, such as fungi, animals and plants. L-type lectins (LTLs) possess a luminal carbohydrate recognition domain which can bind to saccharides. In vertebrates, four kinds of LTLs have been reported, namely endoplasmic reticulum Golgi intermediate compartment-53 (ERGIC-53), ERGIC-53 like protein (ERGL), 36 kDa vesicular integral membrane protein (VIP36), and VIP36 like protein (VIPL) [1]. However, only ERGIC-53 and VIP36 were identified in invertebrates [2].";
  // final static String text = QueryParserBase.escape(inputText);

  List<EntityAnnotation> expectedAnnotations = new ArrayList<>();

  Entity mockCrayfish = new Entity("crayfish", "http://purl.obolibrary.org/obo/NCBITaxon_6724");
  Entity mockLectin = new Entity("lectin", "http://purl.obolibrary.org/obo/GO_0005530");

  List<EntityAnnotation> annotationList = new ArrayList<>();

  @Before
  public void setUp() throws Exception {
    when(config.getDataAttrName()).thenReturn("data-entity");
    EntityRecognizer recognizer = mock(EntityRecognizer.class);

    when(recognizer.getEntities("crayfish", config)).thenReturn(singleton(mockCrayfish));
    when(recognizer.getEntities("lectin", config)).thenReturn(singleton(mockLectin));
    when(recognizer.getCssClass()).thenReturn("mock");
    processor = new EntityProcessorImpl(recognizer);

    expectedAnnotations.add(new EntityAnnotation(mockLectin, 15, 21));
    expectedAnnotations.add(new EntityAnnotation(mockCrayfish, 79, 87));
    expectedAnnotations.add(new EntityAnnotation(mockCrayfish, 220, 229));
    expectedAnnotations.add(new EntityAnnotation(mockCrayfish, 456, 465));
    expectedAnnotations.add(new EntityAnnotation(mockLectin, 1466, 1472));
    expectedAnnotations.add(new EntityAnnotation(mockLectin, 1484, 1492));
  }

  @Test
  public void testGetAnnotations() throws IOException, InterruptedException {
    List<EntityAnnotation> annotationsFromGetAnnotations = processor.getAnnotations(text, config);
    List<EntityAnnotation> annotationsFromAnnotateEntities = processor.annotateEntities(new EntityFormatConfiguration.Builder(new StringReader(text))
      .writeTo(new Writer() {
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
          System.err.println("Annotated text: " + String.valueOf(cbuf, off, len) + ".");
        }

        @Override
        public void flush() throws IOException {

        }

        @Override
        public void close() throws IOException {

        }
      })
      .get()
    );
    annotationsFromGetAnnotations.forEach(annotation -> {
      System.err.println(" - Found annotation using getAnnotations " + annotation + " in <" + text.substring(annotation.range.lowerEndpoint(), annotation.range.upperEndpoint()) + ">");
    });
    annotationsFromAnnotateEntities.forEach(annotation -> {
      System.err.println(" - Found annotation using annotateEntities " + annotation + " in <" + text.substring(annotation.range.lowerEndpoint(), annotation.range.upperEndpoint()) + ">");
    });
    assertThat(annotationsFromGetAnnotations, equalTo(expectedAnnotations));
    assertThat(annotationsFromAnnotateEntities, equalTo(expectedAnnotations));
    assertThat(annotationsFromGetAnnotations, equalTo(annotationsFromAnnotateEntities));
  }
}
