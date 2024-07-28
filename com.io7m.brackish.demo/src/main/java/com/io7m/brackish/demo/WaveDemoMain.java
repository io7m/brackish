/*
 * Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */


package com.io7m.brackish.demo;

import com.io7m.brackish.core.WaveRenderStyle;
import com.io7m.brackish.core.WaveView;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * A waveform demo.
 */

public final class WaveDemoMain
{
  private static final Logger LOG =
    LoggerFactory.getLogger(WaveDemoMain.class);

  private final Stage stage;
  private VBox root;
  private Scene scene;
  private WaveView waveView;
  private AnchorPane waveContainer;
  private Slider zoomSlider;
  private ChoiceBox<WaveDemoModelType> dataSelector;

  private WaveDemoMain(
    final Stage inStage)
  {
    this.stage = Objects.requireNonNull(inStage, "stage");
  }

  /**
   * A waveform demo.
   *
   * @param args The command-line arguments
   */

  public static void main(
    final String[] args)
  {
    Platform.startup(() -> {
      try {
        final var stage = new Stage();
        stage.setMinWidth(640);
        stage.setMinHeight(400);

        final var demo = new WaveDemoMain(stage);
        demo.start();
      } catch (final Throwable e) {
        LOG.error("Error: ", e);
        Platform.exit();
      }
    });
  }

  private void start()
  {
    this.waveView = new WaveView();

    this.root = new VBox();
    this.waveContainer = new AnchorPane();
    this.root.getChildren().add(this.waveContainer);
    VBox.setVgrow(this.waveContainer, Priority.ALWAYS);

    this.waveContainer.getChildren().add(this.waveView);
    AnchorPane.setTopAnchor(this.waveView, 8.0);
    AnchorPane.setBottomAnchor(this.waveView, 8.0);
    AnchorPane.setLeftAnchor(this.waveView, 8.0);
    AnchorPane.setRightAnchor(this.waveView, 8.0);

    this.zoomSlider = new Slider();
    this.zoomSlider.setMax(4096.0);
    this.zoomSlider.setMin(1.0);
    this.zoomSlider.setSnapToTicks(true);
    this.zoomSlider.setShowTickLabels(true);
    this.zoomSlider.setShowTickMarks(true);
    this.root.getChildren().add(this.zoomSlider);
    VBox.setMargin(this.zoomSlider, new Insets(8.0));

    this.dataSelector = new ChoiceBox<>();
    this.dataSelector.setItems(FXCollections.observableList(
      List.of(
        new WaveMonoSine(4096),
        new WaveMonoNoise(4096),
        new WaveStereoNoise(4096)
      )
    ));
    this.root.widthProperty()
      .subscribe((oldValue, newValue) -> {
        this.dataSelector.setPrefWidth(newValue.doubleValue());
      });

    this.root.getChildren().add(this.dataSelector);
    VBox.setMargin(this.dataSelector, new Insets(8.0));

    this.scene = new Scene(this.root);
    this.stage.setScene(this.scene);
    this.stage.show();

    this.waveView.setRenderStyle(WaveRenderStyle.WAVE_BOXES);
    this.waveView.setViewRange(0L, 1024L);

    this.zoomSlider.valueProperty()
      .addListener((observable, oldValue, newValue) -> {
        this.waveView.setViewRange(0L, newValue.longValue());
      });

    this.dataSelector.getSelectionModel()
      .selectedItemProperty()
      .addListener((observable, oldValue, newValue) -> {
        this.waveView.setWaveModel(newValue);
      });

    this.dataSelector.getSelectionModel()
      .selectFirst();

    final var animation =
      new AnimationTimer()
      {
        @Override
        public void handle(
          final long now)
        {
          dataSelector.getSelectionModel()
            .getSelectedItem()
            .update();
          waveView.redraw();
        }
      };

    animation.start();
  }
}
