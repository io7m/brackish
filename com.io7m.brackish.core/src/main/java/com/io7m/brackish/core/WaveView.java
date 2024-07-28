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


package com.io7m.brackish.core;

import com.io7m.brackish.core.internal.WaveChannelView;
import com.io7m.brackish.core.internal.WaveModelEmpty;
import com.io7m.jranges.RangeInclusiveL;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.scene.control.Control;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.HashSet;
import java.util.Objects;

/**
 * A waveform view.
 */

public final class WaveView extends VBox
{
  private static final StyleablePropertyFactory<WaveView> CSS_FACTORY =
    new StyleablePropertyFactory<>(Control.getClassCssMetaData());

  private static final CssMetaData<WaveView, Paint> CSS_WAVE_BACKGROUND =
    CSS_FACTORY.createPaintCssMetaData(
      "waveform-background",
      s -> s.waveformBackgroundColor,
      Color.gray(0.3),
      false
    );

  private static final CssMetaData<WaveView, Paint> CSS_WAVE_CENTER_LINE =
    CSS_FACTORY.createPaintCssMetaData(
      "waveform-center-line",
      s -> s.waveformCenterLineColor,
      Color.color(0.0, 0.0, 1.0),
      false
    );

  private static final CssMetaData<WaveView, Paint> CSS_WAVE_EXPANDED_SAMPLE_STROKE =
    CSS_FACTORY.createPaintCssMetaData(
      "waveform-expanded-sample-stroke",
      s -> s.waveformExpandedSampleStroke,
      Color.gray(1.0),
      false
    );

  private static final CssMetaData<WaveView, Paint> CSS_WAVE_EXPANDED_SAMPLE_FILL =
    CSS_FACTORY.createPaintCssMetaData(
      "waveform-expanded-sample-fill",
      s -> s.waveformExpandedSampleFill,
      Color.gray(0.9),
      false
    );

  private static final CssMetaData<WaveView, Paint> CSS_WAVE_COLLAPSED_SAMPLE_FILL =
    CSS_FACTORY.createPaintCssMetaData(
      "waveform-collapsed-sample-fill",
      s -> s.waveformCollapsedSampleFill,
      Color.gray(1.0),
      false
    );

  private final SimpleObjectProperty<WaveModelType> model;
  private final SimpleObjectProperty<RangeInclusiveL> viewRange;
  private final SimpleObjectProperty<WaveRenderStyle> renderStyle;

  private final HashSet<ReadOnlyProperty<?>> properties;
  private final SimpleStyleableObjectProperty<Paint> waveformBackgroundColor;
  private final SimpleStyleableObjectProperty<Paint> waveformCenterLineColor;
  private final SimpleStyleableObjectProperty<Paint> waveformExpandedSampleFill;
  private final SimpleStyleableObjectProperty<Paint> waveformExpandedSampleStroke;
  private final SimpleStyleableObjectProperty<Paint> waveformCollapsedSampleFill;

  /**
   * A waveform view.
   */

  public WaveView()
  {
    this.model =
      new SimpleObjectProperty<>();
    this.viewRange =
      new SimpleObjectProperty<>(RangeInclusiveL.of(0L, 0L));
    this.renderStyle =
      new SimpleObjectProperty<>(WaveRenderStyle.WAVE_INTERPOLATE_LINEAR);

    this.properties =
      new HashSet<>();
    this.waveformBackgroundColor =
      propertyOf(this, CSS_WAVE_BACKGROUND);
    this.waveformCenterLineColor =
      propertyOf(this, CSS_WAVE_CENTER_LINE);
    this.waveformExpandedSampleFill =
      propertyOf(this, CSS_WAVE_EXPANDED_SAMPLE_FILL);
    this.waveformExpandedSampleStroke =
      propertyOf(this, CSS_WAVE_EXPANDED_SAMPLE_STROKE);
    this.waveformCollapsedSampleFill =
      propertyOf(this, CSS_WAVE_COLLAPSED_SAMPLE_FILL);

    this.setWaveModel(new WaveModelEmpty());
  }

  private static <T> SimpleStyleableObjectProperty<T> propertyOf(
    final WaveView control,
    final CssMetaData<WaveView, T> metadata)
  {
    final var name =
      metadata.getProperty();
    final var prop =
      new SimpleStyleableObjectProperty<>(metadata, control, name);
    prop.setValue(metadata.getInitialValue(control));
    control.properties.add(prop);
    return prop;
  }

  private static RangeInclusiveL adaptViewRange(
    final RangeInclusiveL range,
    final WaveModelType newModel)
  {
    final var size =
      newModel.frameCount();
    final var indexMax =
      Math.max(0L, size - 1L);

    if (range.lower() > indexMax) {
      return RangeInclusiveL.of(0L, indexMax);
    }

    if (range.upper() > indexMax) {
      return RangeInclusiveL.of(range.lower(), indexMax);
    }

    return range;
  }

  /**
   * @return The fill used for collapsed samples
   */

  public Paint waveformCollapsedSampleFill()
  {
    return this.waveformCollapsedSampleFill.get();
  }

  /**
   * @return The fill used for collapsed samples
   */

  public SimpleStyleableObjectProperty<Paint> waveformCollapsedSampleFillProperty()
  {
    return this.waveformCollapsedSampleFill;
  }

  /**
   * @return The fill used for expanded samples
   */

  public Paint waveformExpandedSampleFill()
  {
    return this.waveformExpandedSampleFill.get();
  }

  /**
   * @return The fill used for expanded samples
   */

  public SimpleStyleableObjectProperty<Paint> waveformExpandedSampleFillProperty()
  {
    return this.waveformExpandedSampleFill;
  }

  /**
   * @return The stroke used for expanded samples
   */

  public Paint waveformExpandedSampleStroke()
  {
    return this.waveformExpandedSampleStroke.get();
  }

  /**
   * @return The stroke used for expanded samples
   */

  public SimpleStyleableObjectProperty<Paint> waveformExpandedSampleStrokeProperty()
  {
    return this.waveformExpandedSampleStroke;
  }

  /**
   * @return The stroke used for the center line
   */

  public Paint waveformCenterLineColor()
  {
    return this.waveformCenterLineColor.get();
  }

  /**
   * @return The stroke used for the center line
   */

  public SimpleStyleableObjectProperty<Paint> waveformCenterLineColorProperty()
  {
    return this.waveformCenterLineColor;
  }

  /**
   * @return The fill used for the waveform background
   */

  public Paint waveformBackgroundColor()
  {
    return this.waveformBackgroundColor.get();
  }

  /**
   * @return The fill used for the waveform background
   */

  public SimpleStyleableObjectProperty<Paint> waveformBackgroundColorProperty()
  {
    return this.waveformBackgroundColor;
  }

  /**
   * Set the rendering style.
   *
   * @param style The type
   */

  public void setRenderStyle(
    final WaveRenderStyle style)
  {
    this.renderStyle.set(Objects.requireNonNull(style, "style"));
  }

  /**
   * @return The current rendering style
   */

  public WaveRenderStyle renderStyle()
  {
    return this.renderStyle.get();
  }

  /**
   * @return The current rendering style
   */

  public ReadOnlyObjectProperty<WaveRenderStyle> renderStyleProperty()
  {
    return this.renderStyle;
  }

  /**
   * @return The current waveform view range
   */

  public RangeInclusiveL viewRange()
  {
    return this.viewRange.get();
  }

  /**
   * @return The current waveform view range
   */

  public ReadOnlyObjectProperty<RangeInclusiveL> viewRangeProperty()
  {
    return this.viewRange;
  }

  /**
   * Set the wave model for the view.
   *
   * @param newModel The new wave model
   */

  public void setWaveModel(
    final WaveModelType newModel)
  {
    Objects.requireNonNull(newModel, "newModel");

    this.model.set(newModel);
    this.viewRange.set(adaptViewRange(this.viewRange.get(), newModel));

    final var children = this.getChildren();
    children.clear();

    for (var channel = 0; channel < newModel.channelCount(); ++channel) {
      final var view = new WaveChannelView(this, channel);
      children.add(view);
      VBox.setVgrow(view, Priority.ALWAYS);
    }
  }

  /**
   * @return The current underlying wave model
   */

  public WaveModelType model()
  {
    return this.model.get();
  }

  /**
   * Redraw the view now.
   */

  public void redraw()
  {
    for (final var child : this.getChildren()) {
      if (child instanceof final WaveChannelView view) {
        view.redraw();
      }
    }
  }

  /**
   * Set the view range. This represents the range of frames within the wave
   * model that will appear onscreen.
   *
   * @param lower The lower frame index
   * @param upper The upper frame index
   */

  public void setViewRange(
    final long lower,
    final long upper)
  {
    this.viewRange.set(RangeInclusiveL.of(lower, upper));
  }
}
