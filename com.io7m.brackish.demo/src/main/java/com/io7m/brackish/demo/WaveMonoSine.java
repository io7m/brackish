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

/**
 * A demo waveform consisting of a sine wave.
 */

public final class WaveMonoSine implements WaveDemoModelType
{
  private final double[] data;
  private final double frequency;

  /**
   * A demo waveform consisting of a sine wave.
   *
   * @param size The number of samples
   */

  public WaveMonoSine(
    final int size)
  {
    this.data = new double[size];
    this.frequency = 0.0001;
    this.update();
  }

  @Override
  public void update()
  {
    for (int index = 0; index < this.data.length; ++index) {
      final var t = ((double) index / (double) this.data.length) * 64.0;
      this.data[index] = Math.sin((Math.PI * 2.0 * this.frequency) + t);
    }
  }

  @Override
  public long frameCount()
  {
    return Integer.toUnsignedLong(this.data.length);
  }

  @Override
  public int channelCount()
  {
    return 1;
  }

  @Override
  public double sample(
    final int channel,
    final long frameIndex)
  {
    return this.data[Math.toIntExact(frameIndex)];
  }

  @Override
  public String toString()
  {
    return "WaveMonoSine";
  }

  @Override
  public double sampleOrDefault(
    final int channel,
    final long frameIndex,
    final double orElse)
  {
    try {
      return this.sample(channel, frameIndex);
    } catch (final Exception e) {
      return orElse;
    }
  }
}
