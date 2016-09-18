package client

import game._

import com.thoughtworks.binding.Binding.{Var, Constants, BindingSeq}
import com.thoughtworks.binding.dom.Runtime.TagsAndTags2
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.html.Div

object Board {
  @dom def apply(position: Var[Position[Point19]]): Binding[Div] = {
    <div style="
      width: 684px;
      height: 684px;
      background-color: #e4bb67;
      border: 8px solid #e4bb67;
      position: relative;
      float: left;
      display: inline;
    ">
      { goban(36).bind }
      <div style="
        width: 684px;
        height: 684px;
        position: absolute;
        zIndex: 2;
      ">
        { stones(position).bind }
      </div>
    </div>
  }

  @dom def stones(position: Var[Position[Point19]]): Binding[BindingSeq[Div]] =
    Constants(Shape[Point19].all: _*).map { p =>
      Client.position.bind.at(p) match {
        case Empty        => <div class="empty"></div>
        case Stone(Black) => <div class="black"></div>
        case Stone(White) => <div class="white"></div>
      }
    }

  @dom def goban(u: Int) = {
    implicit def toSvgTags(a: TagsAndTags2.type) = scalatags.JsDom.svgTags

    val w = 18 * u
    val c = u / 2.0 + 0.5
    val gray = "#111"

    val viewBox =s"-$c, -$c, ${w + u}, ${w + u}"

    val hp = s"m 0 $u h  $w "
    val hm = s"m 0 $u h -$w "
    val vp = s"m $u 0 v  $w "
    val vm = s"m $u 0 v -$w "

    val grid = s"M 0 0 h $w $hm  ${(hp + hm) * 8} M 0 0 v $w $vm ${(vp + vm) * 8}"

    val pr = s"m  ${u * 6} 0 l 0 0"
    val pl = s"m -${u * 6} 0 l 0 0"
    val pd = s"m 0  ${u * 6} l 0 0"

    val hoshis = s"m ${u * 3} ${u * 3} l 0 0 $pr $pr $pd $pl $pl $pd $pr $pr"

    <svg data:viewBox={viewBox} style:position="absolute" style:zIndex="0">
      <rect
        data:height={w.toString}
        data:width={w.toString}
        data:stroke={gray}
        data:stroke-width="1"
        data:fill="none">
      </rect>
      <path
        data:stroke={gray}
        data:stroke-width="1"
        data:fill="none"
        data:d={grid}>
      </path>
      <path
        data:stroke={gray}
        data:stroke-width="5"
        data:stroke-linecap="round"
        data:d={hoshis}>
      </path>
    </svg>
  }
}
