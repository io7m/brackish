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


package com.io7m.brackish.core.internal;

import com.io7m.brackish.core.WaveModelType;
import com.io7m.brackish.core.WaveView;
import com.io7m.jaffirm.core.Invariants;
import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jranges.RangeInclusiveL;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.util.Objects;

/**
 * A view of a single channel.
 */

public final class WaveChannelView extends Canvas
{
  private final int channelIndex;
  private final WaveView waveView;

  /**
   * A view of a single channel.
   *
   * @param inView         The parent wave view
   * @param inChannelIndex The channel index of this view
   */

  public WaveChannelView(
    final WaveView inView,
    final int inChannelIndex)
  {
    this.waveView =
      Objects.requireNonNull(inView, "waveModel");
    this.channelIndex =
      inChannelIndex;
  }

  @Override
  public boolean isResizable()
  {
    return true;
  }

  @Override
  public double maxHeight(
    final double width)
  {
    return Double.POSITIVE_INFINITY;
  }

  @Override
  public double maxWidth(
    final double height)
  {
    return Double.POSITIVE_INFINITY;
  }

  @Override
  public double minWidth(
    final double height)
  {
    return 1.0;
  }

  @Override
  public double minHeight(
    final double width)
  {
    return 1.0;
  }

  @Override
  public void resize(
    final double width,
    final double height)
  {
    this.setWidth(width);
    this.setHeight(height);
    this.render(this.getGraphicsContext2D());
  }

  private void render(
    final GraphicsContext g)
  {
    final var model =
      this.waveView.model();
    final var viewRange =
      this.waveView.viewRange();

    final var w = this.getWidth();
    final var h = this.getHeight();

    g.setFill(this.waveView.waveformBackgroundColor());
    g.fillRect(0, 0, w, h);

    /*
     * If the number of frames in the view range is greater than the width
     * of the canvas, then this means that we will have more waveform frames
     * than will fit on the canvas. This implies that we need to render in
     * "collapsed" form; multiple waveform frames will be combined to form
     * a single sample on the canvas.
     *
     * Otherwise, the number of available pixels is greater than or equal
     * to the number of waveform frames. This implies that each frame in
     * the waveform will take up one or more pixels onscreen, hence rendering
     * in "expanded" form.
     */

    final var interval = viewRange.interval();
    if (interval <= 1L) {
      return;
    }

    if (interval > w) {
      this.renderCollapsed(g, model, viewRange);
    } else {
      this.renderExpanded(g, model, viewRange);
    }
  }

  private void renderCollapsed(
    final GraphicsContext g,
    final WaveModelType model,
    final RangeInclusiveL viewRange)
  {
    final var w =
      this.getWidth();
    final var h =
      this.getHeight();
    final var maxFrames =
      viewRange.interval();

    Preconditions.checkPreconditionV(
      maxFrames > w,
      "Frame count %s must be greater than view width %s"
        .formatted(maxFrames, w)
    );

    final var framesPerPixel =
      (double) viewRange.interval() / w;

    Invariants.checkInvariantD(
      framesPerPixel,
      x -> x >= 1,
      x -> "Frames per pixel must be >= 1"
    );

    final var halfHeight = h / 2.0;

    try {
      g.save();
      g.translate(0, halfHeight);
      g.setStroke(this.waveView.waveformCenterLineColor());
      g.strokeLine(0, 0.0, w, 0.0);

      g.setFill(this.waveView.waveformCollapsedSampleFill());

      for (int x = 0; x < w; ++x) {
        final var frameIndex =
          (double) viewRange.lower() + (x * framesPerPixel);

        var sampleMax = 0.0;
        var sampleMin = 0.0;

        for (double k = 0.0; k <= framesPerPixel; ++k) {
          final var sample =
            model.sampleLerp(this.channelIndex, frameIndex + k);
          sampleMax = Math.max(sampleMax, sample);
          sampleMin = Math.min(sampleMin, sample);
        }

        final var yTop =
          sampleMax * -halfHeight;
        final var yBottom =
          sampleMin * halfHeight;

        g.fillRect(x, yTop, 1.0, Math.abs(yTop));
        g.fillRect(x, 0.0, 1.0, Math.abs(yBottom));
      }

    } finally {
      g.restore();
    }
  }

  private void renderExpanded(
    final GraphicsContext g,
    final WaveModelType model,
    final RangeInclusiveL viewRange)
  {
    switch (this.waveView.renderStyle()) {
      case WAVE_INTERPOLATE_LINEAR -> {
        this.renderExpandedLinear(g, model, viewRange);
      }
      case WAVE_BOXES -> {
        this.renderExpandedBoxes(g, model, viewRange);
      }
    }
  }

  private void renderExpandedBoxes(
    final GraphicsContext g,
    final WaveModelType model,
    final RangeInclusiveL viewRange)
  {
    final var w = this.getWidth();
    final var h = this.getHeight();

    final var pixelsPerFrame =
      w / (double) viewRange.interval();

    Invariants.checkInvariantD(
      pixelsPerFrame,
      x -> x >= 1,
      x -> "Pixels per frame must be >= 1"
    );

    try {
      final var halfHeight = h / 2.0;

      g.save();
      g.translate(0, halfHeight);
      g.setStroke(this.waveView.waveformCenterLineColor());
      g.strokeLine(0, 0.0, w, 0.0);

      g.setStroke(this.waveView.waveformExpandedSampleStroke());
      g.setFill(this.waveView.waveformExpandedSampleFill());

      for (double x = 0; x < w; x += pixelsPerFrame) {
        final var position =
          x / w;
        final var p0 =
          viewRange.lower() * (1 - position);
        final var p1 =
          viewRange.upper() * position;
        final var frameIndex =
          p0 + p1;

        final var s0 =
          model.sampleLerp(this.channelIndex, frameIndex);
        final var height =
          Math.abs(s0 * halfHeight);

        if (s0 > 0.0) {
          final var y0 = s0 * -halfHeight;
          g.fillRect(x, y0, pixelsPerFrame, height);
          g.strokeRect(x, y0, pixelsPerFrame, height);
        } else {
          g.fillRect(x, 0.0, pixelsPerFrame, height);
          g.strokeRect(x, 0.0, pixelsPerFrame, height);
        }
      }
    } finally {
      g.restore();
    }
  }

  private void renderExpandedLinear(
    final GraphicsContext g,
    final WaveModelType model,
    final RangeInclusiveL viewRange)
  {
    final var w = this.getWidth();
    final var h = this.getHeight();

    final var pixelsPerFrame =
      w / (double) viewRange.interval();

    Invariants.checkInvariantD(
      pixelsPerFrame,
      x -> x >= 1,
      x -> "Pixels per frame must be >= 1"
    );

    try {
      final var halfHeight = h / 2.0;

      g.save();
      g.translate(0, halfHeight);
      g.setStroke(this.waveView.waveformCenterLineColor());
      g.strokeLine(0, 0.0, w, 0.0);

      g.setStroke(this.waveView.waveformExpandedSampleStroke());
      g.setFill(this.waveView.waveformExpandedSampleFill());

      final var polyPointsX = new double[4];
      final var polyPointsY = new double[4];

      for (double x = 0; x < w; x += pixelsPerFrame) {
        final var position =
          x / w;
        final var p0 =
          viewRange.lower() * (1 - position);
        final var p1 =
          viewRange.upper() * position;
        final var frameIndex =
          p0 + p1;

        final var frameIndex0 =
          (long) Math.floor(frameIndex);
        final var frameIndex1 =
          (long) Math.ceil(frameIndex);

        final var s0 =
          model.sampleOrDefault(this.channelIndex, frameIndex0, 0.0);
        final var s1 =
          model.sampleOrDefault(this.channelIndex, frameIndex1, 0.0);

        polyPointsX[0] = x;
        polyPointsX[1] = x;
        polyPointsX[2] = x + pixelsPerFrame;
        polyPointsX[3] = x + pixelsPerFrame;

        polyPointsY[0] = 0.0;
        polyPointsY[1] = s0 * -halfHeight;
        polyPointsY[2] = s1 * -halfHeight;
        polyPointsY[3] = 0.0;

        g.fillPolygon(polyPointsX, polyPointsY, 4);
        g.strokePolygon(polyPointsX, polyPointsY, 4);
      }
    } finally {
      g.restore();
    }
  }

  /**
   * Redraw the view now.
   */

  public void redraw()
  {
    this.render(this.getGraphicsContext2D());
  }
}
