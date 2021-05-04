
import 'dart:math';
import 'dart:convert';
import 'package:flutter/material.dart';//flutter part too

part 'kwidgets.dart';

class PseudoRandom {
  int _a;
  int _c;
  int _m = 1 << 32;
  int _s;
  int _i;

  PseudoRandom([int prod = 1664525, int add = 1013904223]) {
    _a = prod;
    _c = add;
    _s = Random().nextInt(_m) * 2 + 1;//odd
    next();// a fast round
    _i = _a.modInverse(_m);//4276115653 as inverse of 1664525
  }

  int next() {
    return _s = (_a * _s + _c) % _m;
  }

  int prev() {
    return _s = (_s - _c) * _i % _m;
  }
}

class _RingNick {
  //alternate strategy of using least and most and lower window
  static bool alt = true;
  static double biasPlus = 0.125;//gen better figure
  final List<double> walls = [ 0.25 - biasPlus, 0.5, 0.75 + biasPlus ];
  int _position = 0;
  final int mostEscaped = alt ? 0 : 1;//the lowest pair of walls 0.25 and 0.5
  final int leastEscaped = 2;//the highest walls 0.5 and 0.75
  final int theThird = alt ? 1 : 0;//the 0.75 and 0.25 walls
  bool _right = true;
  static final PseudoRandom _pr = PseudoRandom();

  int _getPosition() => _position;

  int _asMod(int pos) {
    return pos % walls.length;
  }

  void _setPosition(int pos) {
    _position = _asMod(pos);
  }

  void _next() {
    int direction = _right ? 0 : walls.length - 1;//truncate to 2
    double wall = walls[_asMod(_getPosition() + direction)];
    if(_pr.next() > (wall * _pr._m).toInt()) {
      //jumped
      _setPosition(_position + (_right ? 1 : walls.length - 1));
    } else {
      //not jumped
      _right = !_right;//bounce
    }
  }

  void _prev() {
    int direction = !_right ? 0 : walls.length - 1;//truncate to 2
    double wall = walls[_asMod(_getPosition() + direction)];
    if(_pr._s > (wall * _pr._m).toInt()) {// the jump over before sync
      //jumped
      _setPosition(_position + (!_right ? 1 : walls.length - 1));
    } else {
      //not jumped
      _right = !_right;//bounce -- double bounce and scale before sync
    }
    _pr.prev();//exact inverse
  }

  void __next(bool skip) {
    _next();
    if(!skip) while(_getPosition() == mostEscaped) _next();
  }

  void __prev(bool skip) {
    _prev();
    if(!skip) while(_getPosition() == mostEscaped) _prev();
  }
}

class _GroupHandler {//yes, something goes here
  List<_RingNick> _rn;
  final int _window = 5;
  final int _opportunity = 1;
  final int _favouriteBit = 1 << 27;
  final bool _hypothesisA = true;//mutual information between wall states
  final bool _hypothesisB = true;//mutual information between wall states
  final bool _hypothesisC = true;//no spread required
  final bool _hypothesisD = true;//right bit is better for bias
  //Hypothesis D is perhaps the most interesting one.
  //There was an experiment with 128 walls at one point with the assumption
  //that the higher walls to the right would be more reflective and hence
  //the ring would move left more often.
  //The experiment proved this wrong with central limit statistics after 2 and a bit days ...
  //The explanation was the more "containy" right hand side held the climber
  //closer to the right edge, with an occasional fast zip through the left hand
  //side. The net bias was for rightward motion in the "current code framework"
  //The number 55 was not pulled from thin air, but was the worst case odd "consensus"
  //size of ring nicks.
  //The walls being lower on the left edges of cells was not providing the correct
  //direction of bias in motion.

  _GroupHandler(int size) {
    if(size % 2 == 0) size++;
    _rn = List<_RingNick>(size);
  }

  //BIAS
  //REMAIN (w1 * w2)^N
  //LEAVE (1-w1) + w1 * (1-w2) ...
  //even the switching on the fly of alt?
  //The matrix joke about mining humans as Maxwell demons was quite funny

  //the effect of a _mod1 and its 'right' interaction with the lowest wall?
  //in the most contained, which wall is higher?
  //in the least contained, which?
  //so a more likely turns into ?

  void _next() {
    for(int i = 0; i < _window; i++) {//null bias settling window
      if(!_hypothesisC) if(i == _opportunity) {//1 pre (so 4 of 5)
        for (_RingNick r in _rn) {
          _RingNick._pr.next();
          if((_RingNick._pr._s & _favouriteBit) != 0) {
            //kind of an xor spread to 50:50 cell
            _mod1(r, true);
          }
        }
      }
      for (_RingNick r in _rn)
        r.__next(_hypothesisD);
    }
  }

  void _prev() {
    for(int i = 0; i < _window; i++) {
      for(_RingNick r in _rn.reversed)
        r.__prev(_hypothesisD);
      if(!_hypothesisC) if(i == _window - _opportunity - 1) {//3 post (so 4 of 5)
        for(_RingNick r in _rn.reversed) {
          if((_RingNick._pr._s & _favouriteBit) != 0) {
            _mod1(r, false);
          }
          _RingNick._pr.prev();
        }
      }
    }
  }

  bool _majority() {
    int count = 0;
    for(_RingNick r in _rn) {
      if(_hypothesisD) {
        if(r._right) count++;
      } else {
        if (r._getPosition() == r.leastEscaped) count++; //a main cumulative
      }
    }// the > 2/3rd state would be true
    return (2 * count > _rn.length);
  }

  void _mod1(_RingNick r, bool direction) {
    //if(_hypothesis) r._right = !r._right;// a wall reflection hypothesis!
    //either both are the same or one is worse than the other (non-linear)
    //a more likely turns into ?
    //engineering differential probabilities
    if(_hypothesisD) {
      r._right = !r._right;//basic direction of motion flip
    } else {
      if (r._getPosition() == r.leastEscaped) {
        r._setPosition(r.theThird);
        if ((_hypothesisA ^ direction) || (_hypothesisB ^ !direction))
          r._right = !r._right;
      } else {
        //mostEscaped eliminated by not being used
        r._setPosition(r.leastEscaped);
        if ((_hypothesisB ^ direction) || (_hypothesisA ^ !direction))
          r._right = !r._right;
      }
    }
  }

  void _modulate(bool direction) {
    for(_RingNick r in _rn) _mod1(r, direction);//all modulate
  }
}

class _Modulator {
  final _GroupHandler _gh = _GroupHandler(_RingNick.alt ? 33 : 55);

  int _putBit(bool bitToAbsorb) {//returns absorption status
    _gh._next();
    if(_gh._majority()) {//main zero state
      if(bitToAbsorb) {
        _gh._modulate(true);
        return 0;//a zero yet to absorb
      } else {
        return 1;//absorbed zero
      }
    } else {
      return -1;//no absorption emitted 1
    }
  }

  int _getBit(bool bitLastEmitted) {
    if(_gh._majority()) {//zero
      _gh._prev();
      return 1;//last bit not needed emit zero
    } else {
      if(bitLastEmitted) {
        _gh._prev();
        return -1;//last bit needed and nothing to emit
      } else {
        _gh._modulate(false);
        _gh._prev();
        return 0;//last bit needed, emit 1
      }
    }
  }
}

class StackHandler {
  final List<bool> _data = [];
  final _Modulator _m = _Modulator();
  int _lenCode = 0;

  int _putBits() {
    int count = 0;
    while(_data.length > 0) {
      bool v = _data.removeLast();
      switch(_m._putBit(v)) {
        case -1:
          _data.add(v);
          _data.add(true);
          break;
        case 0:
          _data.add(false);
          break;
        case 1:
          break;//absorbed zero
        default: break;
      }
      count++;
    }
    return count;
  }

  void _getBits(int count) {
    while(count > 0) {
      bool v;
      v = (_data.length == 0 ? false : _data.removeLast());//zeros out
      switch(_m._getBit(v)) {
        case 1:
          _data.add(v);//not needed
          _data.add(false);//emitted zero
          break;
        case 0:
          _data.add(true);//emitted 1 used zero
          break;
        case -1:
          break;//bad skip, ...
        default: break;
      }
      count--;
    }
  }

  //STACK BLOCK FUNCTIONS 32 BIT

  int _putBlock(int lastCount) {
    explode(lastCount);
    return _putBits();
  }

  void putBlock() {
    _lenCode = _putBlock(_lenCode);
  }

  int _getBlock(int count) {
    if(count <= 0) throw Exception('Kodek empty below accumulated depth.');
    _getBits(count);
    if(_data.length < 32) throw Exception('Kodek hacked bad terminal.');//an empty is min 32
    return implode();
  }

  void getBlock() {
    _lenCode = _getBlock(_lenCode);
  }

  //BASIC UNIT OF EXTERNALIZED

  void explode(int x, [int c = 32]) {
    for(int i = 0; i < c; i++)
      _data.add(x & (1 << i) != 0);
  }

  int implode([int c = 32]) {
    int result = 0;
    for(int i = 0; i < c; i++)
      result &= ((_data.removeLast() ?? false) ? 1 : 0) << (c - 1 - i);
    return result;
  }

  void load() {
    for(_RingNick r in _m._gh._rn) {
      if(_data.length <= 0) throw Exception('Bad Kodek block length.');
      while((r._position = implode(2)) == 3) implode(1);//BILBO
      r._right = implode(1) == 1;
    }
    _lenCode = implode();
    _RingNick._pr._s = implode();
  }

  void save(bool reset) {
    explode(_RingNick._pr._s);
    explode(_lenCode);
    for(_RingNick r in _m._gh._rn.reversed) {
      explode(r._position, 2);
      explode(r._right ? 1 : 0, 1);
    }
    if(reset) _lenCode = 0;//reset
  }

  void explodeString(String s) {
    if(s == null) {
      explode(0, 1);
      return;
    }
    List<int> bytes = utf8.encode(s);
    int len = s.length;
    for(int i in bytes) explode(i, 8);
    explode(len);
    explode(1, 1);//not null marker
  }

  String implodeString() {
    if(implode(1) == 0) return null;//string terminal
    int len = implode();
    List<int> bytes = [];
    for(int i = 0; i < len; i++) bytes.add(implode(8));
    return utf8.decode(bytes.reversed);
  }

  String getB64() {
    if(_data.length != 0) throw Exception('Kodek must be fully packed.');
    save(false);
    List<int> chars = [];
    while(_data.length >= 8) {
      chars.add(implode(8));
    }
    int i = _data.length;
    chars.add(implode(8));
    chars.add(i);//length bits
    return base64Encode(chars);
  }

  void setB64(String s) {
    List<int> chars = base64Decode(s);
    int x = chars.removeLast();
    if(x > 7 || x < 0) throw Exception('Bad Kodek block alignment.');
    explode(chars.removeLast(), 8);//8 bits
    while((x++) < 8) implode(1);//remove bits
    while(chars.length > 0) explode(chars.removeLast(), 8);//explode kodek content
    load();
  }
}

class BlockChainTXStack {//A singleton is best, as the interaction between multiples is not order 'safe'
  final StackHandler _k = StackHandler();
  String _saved;

  String send(String s) {
    _k.explodeString(s);
    _k.putBlock();
    return _k.getB64();
  }

  List<String> receive(String s, [int down = 1, bool terminal = true]) {
    List<String> ls = [];
    _k.setB64(s);
    while(down-- > 0) {
      _k.getBlock();//top
      ls.add(_k.implodeString());
    }
    if(!terminal) ls.add(_k.getB64());//might as well make it easy to get a handle on lower ones again?
    return ls;
  }

  String nest() {//does not save push/pop states
    send(_k.getB64());//don't need result yet
    return send(_saved);
  }

  void fledgling(String s, [int nestDown = 1, bool destroyPop = false]) {//in pairs
    List<String> ls = receive(s, 2 * nestDown);//don't need terminal hook
    if(destroyPop) {
      _saved = ls.removeLast();//the saved stack
    } else {
      ls.removeLast();//dump
    }
    _k.setB64(ls.removeLast());//the kodek active
  }

  void push() {//state save
    String tmp = _k.getB64();
    if(_saved != null) _k.setB64(_saved);
    _saved = send(tmp);
    _k.setB64(tmp);//restore
  }

  void pop() {//state reload
    _k.setB64(receive(_saved, 1, false).removeLast());
  }

  void popNest() {
    String k = _k.getB64();//don't need result yet
    String tmp = _saved;
    pop();
    send(k);
    send(tmp);//stack the temporary pushed scope as a nest in the pop scope
  }

  void pushFledgling([int nestDown = 1, bool destroyTree = true]) {
    String s = _k.getB64();
    if(destroyTree) receive(s, 2);//2DROP
    push();//save scope
    fledgling(s, nestDown);//extract from self
  }
}

class BlockTreeManager {
  final BlockChainTXStack tx = BlockChainTXStack();

  void insert() {

  }

  String select() {
    return null;//TODO
  }
}