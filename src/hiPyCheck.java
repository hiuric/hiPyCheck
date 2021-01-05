

import otsu.hiNote.*;
import java.io.*;
import java.util.regex.*;
import java.util.*;
/**
 * 未定義変数をエラーとする
 */
public class hiPyCheck {
   final static boolean D=true;
   final static boolean DD=false;
   final static String  ver="0.0.1";
   static class VarScope {
      HashMap<String,String> vals  =new HashMap<>();
      VarScope               parent=null;
      ArrayList<VarScope>    childs=new ArrayList<>();
      };
   VarScope global;      // ファイルごとに初期化
   VarScope current_def; // 廃棄予定
   int      def_indent;
   VarScope current_scope;
   //
   boolean  start_def;// def xxx()行、次の行でfalse,in_def=true
   boolean  in_def;
   // classスコープは完全無視
   // インデント階層：主に複数階層の戻りの制御のために使う
   Stack<Integer>                    indentNest     = new Stack<>();
   //
   final static String TRIPLE_QUOTE="\"\"\"";
   hiArgs   argv;
   String[] inputs;
   // 設定
   boolean                terse;
   boolean                omit_same;
   boolean                detail;
   boolean                verbose;
   boolean                step;
   boolean                message;
   boolean                redefine_global;
   boolean                no_tent;
   int                    disp_errors;
   // work
   int                    errors;
   String                 current_file;
   int                    current_line_no;
   int                    indent;
   int                    ign;
   HashMap<String,String> self_vals      = null;// クラス定義の中である判断を兼ねる
   String                 className      = "";
   boolean                in_class       = false;
   HashMap<String,String> error_out=new HashMap<>();
   static class MyHelper implements hiArgs.Helper {
      static String[] usage= {
         "hiPyCheck "+ver
        ,"Usage: hiPyCheck [options] .py-file [ .py-files ...]"
        ,"options:"
        ,"   -terse   : ファイル名、エラー数を表示しない"
        ,"   -err 数  : 指定数以上のエラーで処理を終了する"
        ,"   -no_tent : デフォルトでは未定義変数への代入時には"
        ,"              エラーを表示した上で、仮の変数定義を行う"
        ,"              本指定がある場合仮の変数定義は行わない"
        ,"   -step    : 有意の各行に対して解釈を表示する"
        ,"   -verbose : 解析中の情報を表示する"
         };
      public void help(String key_,String msg_){
         hiArgs.Helper.default_help(usage,key_,msg_);
         }
      }
   hiPyCheck(String[] argv_){
      hiArgs   _argv = new hiArgs(argv_);
      _argv.setHelper(new MyHelper());
      if( _argv.argBool("-version",false) ){
         System.out.println("hiPyCheck "+ver);
         System.exit(0);
         }
      terse           = _argv.argBool("-terse",false);
      disp_errors     = _argv.argInt("-err",Integer.MAX_VALUE);
      redefine_global = _argv.argBool("-redefine_global",false);
      no_tent         = _argv.argBool("-no_tent",false);
      omit_same       = _argv.argBool("-omit_same",false);
      detail          = _argv.argBool("-detail",false);
      verbose         = _argv.argBool("-verbose",false);
      step            = _argv.argBool("-step",false);
      message         = _argv.argBool("-message",false);
      inputs          = _argv.other(".py-file",1,100000);
      }

   Pattern CLASS  =Pattern.compile(hiRegex.re('!',"class!s+(!w+)!s*(!(|:)"));
   Pattern ASSIGN =Pattern.compile(hiRegex.re('!',"^([ ]*)([!w.]+)!s*=.*"));
   Pattern VARDEF =Pattern.compile(hiRegex.re('!',"^([ ]*)([!w.]+)!s*:!s*([!w.]+)!s*=.*"));
   Pattern LISTDEF=Pattern.compile(hiRegex.re('!',"^([ ]*)([!w.]+)!s*=!s*![!].*"));
   Pattern GLOBAL =Pattern.compile(hiRegex.re('!',"^([ ]*)global!s+(!w+)"));
   Pattern DEF    =Pattern.compile(hiRegex.re('!',"^([ ]*)def!s+(!w+)"));
   int start()throws Exception{
      int _total_errors=0;
      for(String _input:inputs){
         current_file= _input;
         if(!terse) System.out.println("====== CHCEK : "+current_file+" ======");
         try(BufferedReader _br=hiFile.openTextFileR(_input);){
            check(_br);
            }
         if(!terse){
            if( errors==0 )      System.out.println("- NO ERROR -");
            else if( errors==1 ) System.out.println("- ONE ERROR -");
            else                 System.out.println("- "+errors+" ERRORs -");
            }
         _total_errors+= errors;
         }
      return _total_errors;
      }
   void check(BufferedReader br_)throws Exception{
      String         _line_text;
      String         _prev_line_text="";
      boolean        _in_multiline_string=false;
      int            _previndent=0;

      if( step && !terse ){
         System.out.println("--- "+current_file+" ---");
         }

      errors         =0;
      current_line_no=0;
      ign=-1;
      global= new VarScope();
      current_scope= global;

      indentNest= new Stack<>();
      indentNest.push(0);
      while( (_line_text=br_.readLine())!=null ){
         ++current_line_no;
         //--------------------------------------------------------
         // 引用符内とコメントを空にする
         //--------------------------------------------------------
         _line_text= shrink_line(_line_text,_in_multiline_string);
         //--------------------------------------------------------
         // 三連引用符の終了 チェック
         //--------------------------------------------------------
         if( _in_multiline_string ){
            int _str_sta_pos= _line_text.indexOf(TRIPLE_QUOTE);
            if( _str_sta_pos!=-1 ){
               int _num_of_quotes= count(_line_text,TRIPLE_QUOTE);
               if( _num_of_quotes==1 ){
                  if(verbose)System.out.println(file_line()+"\tMULTILINE STRING END");
                  _in_multiline_string=false;
                  }
               else{
                  message("*** MULTI MULTILINE STRING (at END)***");
                  }
               }
            continue;
            }
         //--------------------------------------------------------
         // 継続行を繋げる
         //--------------------------------------------------------
         _line_text=hiText.trimLast(_line_text);
         if( _line_text.length()==0 ){
            // _prev_line_text=_line_text;
            continue;
            }
         while( _line_text.endsWith("\\") ){
            String _continued_line;
            _line_text=hiText.trimLast(_line_text,1);
            if( (_continued_line=br_.readLine())==null )break;
            ++current_line_no;
            _line_text+=hiText.trim(_continued_line);// 継続行は前後をトリム
            }
         //--------------------------------------------------------
         // インデント取得:Pythonの核心（あほらしいと思うが)
         //--------------------------------------------------------
         for(indent=0;indent<_line_text.length();++indent){
            if( _line_text.charAt(indent)!=' ' )break;
            }
         String _ext_without_indent=_line_text.substring(indent).toUpperCase();
         //----------------------------------------------------------------------------
         // 三連引用符の開始
         //   """           開始
         //   x="""         開始
         //   x="""aaa      開始
         //   x="""aaa"""   開始としない
         //   x="""aaa """ """ 不可
         // --⤴
         //----------------------------------------------------------------------------
         int _str_sta_pos= _line_text.indexOf(TRIPLE_QUOTE);
         if( _str_sta_pos!=-1 ){
            int _com_sta_pos= _line_text.indexOf('#');
            if( _com_sta_pos==-1 || _com_sta_pos>_str_sta_pos ){
               int _num_of_quotes= count(_line_text,TRIPLE_QUOTE);
               if( _num_of_quotes==1 ){
                  _in_multiline_string=true;
                  if( _str_sta_pos==indent) {
                     if(verbose)System.out.println(file_line()+"\tMULTILINE STRING START (SIMPLE)");
                     // この行の解析の必要はない
                     continue;
                     }
                  else{
                     if(verbose)System.out.println(file_line()+"\tMULTILINE STRING COMMENT START (IN-LINE)");
                     // a="""形式の可能性があるのでこの行は解析する
                     }
                  }
               else if( _num_of_quotes==2 ){
                  if(verbose)System.out.println(file_line()+"\tMULTILINE STRING START/END");
                  // a=""" abc """ 形式の可能性があるのでこの行は解析する
                  }
               else{
                  message("*** MULTI MULTILINE STRING (at START)***");
                  continue;
                  }
               //continue;
               }
            }
         //---------------------------------------------------
         // 継続行 変数チェックは行わない
         // --⤴
         //---------------------------------------------------
         if(_prev_line_text.endsWith(",")  ){
            _prev_line_text=_line_text;
            _previndent=indent;
            continue;
            }
         _prev_line_text=_line_text;
         if( _ext_without_indent.startsWith(",") ){
            _previndent=indent;
            continue;
            }
         if( hiText.trim(_line_text).length()==0 ) continue;
         //=============================================================
         // 有効行
         //===================================================
         if( verbose||step ){
            System.out.println(line()+">\t"+_line_text);
            if( indent>_previndent ){
               System.out.println(line()
                                  +"\tINDENT CHANGED "
                                  +_previndent+" -> "+indent);
               }
            else if( indent<_previndent ){
               System.out.println(line()
                                  +"\tINDENT CHANGED "
                                  +indent+" <- "+_previndent);
               }
            }
         //---------------------------------------------------
         // class
         // ・クラスレベルではスコープを持たせない
         //   クラス変数はチェック対象外
         // --⤴
         //---------------------------------------------------
         Matcher _class =CLASS.matcher(_line_text);
         if(_class.find()) {
            className=_class.group(1);
            if(verbose||step) System.out.println(line()+"\tCLASS START "+className);
            self_vals    = new HashMap<>();
            in_class     = true;
            //in_outer_def = false;
            _previndent  =indent;
            current_def  =null;
            current_scope=null;
            continue;
            }
         //---------------------------------------------------
         // def
         // 最外層/クラス内　どちらもここ
         // ここでは開始フラグ(start_def)を設定し
         // スコープインスタンスは次行で設定
         // --⤴
         //---------------------------------------------------
         Matcher _def= DEF.matcher(_line_text);
         if( _def.find() ){
            String _func=_def.group(2);
            if(verbose||step) System.out.println(line()+"\tDEF "+_func);
            //if(verbose&&detail)System.out.println("DEF "+_func+" indent="+indent);
            //if( indent==0 ) in_outer_def=true;//廃棄予定
            def_indent= indent;
            start_def=true;
            _previndent=indent;
            continue;
            } 
         //---------------------------------------------------
         // インデントによるスコープ制御
         //---------------------------------------------------
         if( indent>_previndent ){
            indentNest.push(indent);
            // 一段深くなる
            if( start_def ){
               start_def    = false;
               current_def  = new VarScope();
               current_scope= current_def;
               in_def       = true;
               }
            else if( current_scope!=null ){
               VarScope _newScope= new VarScope();
               _newScope.parent= current_scope;
               current_scope.childs.add(_newScope);
               current_scope= _newScope;
               }
            }
         else if( indent<_previndent ){
            // 一段浅くなる

            while( indentNest.peek()>indent ){
               if( current_scope!=null ){
                  current_scope= current_scope.parent;
                  }
               if( indent==def_indent ){
                  in_def=false;
                  current_def=null;
                  current_scope=null;
                  }
               int _n=indentNest.pop();
               }
            if( indent==0 ){
               current_def= global;
               current_scope= current_def;
               }
            }
         _previndent=indent;
         //---------------------------------------------------
         // global
         // --⤴
         //---------------------------------------------------
         Matcher _global= GLOBAL.matcher(_line_text);
         if( _global.find() ){
            String _var=_global.group(2);
            if(verbose)System.out.println("GLOBAL MATCH "+_var);
            setVarType(_var,"#global");
            continue;
            } 
         //---------------------------------------------------
         // 単純割り当て
         // --⤴
         //---------------------------------------------------
         Matcher _assign =ASSIGN.matcher(_line_text);
         if(_assign.find()) {
            String _var=_assign.group(2);
            if(verbose&&detail)System.out.println("ASSIGN MATCH "+_var);
            if( ign==current_line_no-1 ){
               // #ignが前行にある。チェックしない
               if(verbose&&detail)System.out.println(file_line()+"\t#ign\f"+_var);
               //setVarType(_var,"any");
               }
            else {
               //  a        = x チェック
               //  a.b      = x チェックしない
               //  self.a   = x  チェック
               //  self.a.b = x チェックしない
               int _pos=_var.indexOf('.');
               if( _pos==-1 ) getType(_var);
               else if( _var.startsWith("self") ){
                  if( count(_var,'.')==1 ) getType(_var);
                  }
               }
            continue;
            }
         //---------------------------------------------------
         // 型付き割り当て
         // --⤴
         //---------------------------------------------------
         Matcher _vardef =VARDEF.matcher(_line_text);
         if(_vardef.find()) {
            if(verbose)System.out.println("VARDEF MATCH");
            String _var = _vardef.group(2);
            if( ign==current_line_no-1 ){
               // #ignが前行にある。チェックしない
               if(verbose&&detail)System.out.println(file_line()+"\t#ign\f"+_var);
               //setVarType(_var,"any");
               }
            else{
               String _type= _vardef.group(3);
               setVarType(_var,_type);
               }
            continue;
            }
         }
      }
   /**
    * 引用符内と#から改行までを消去する
    */
   String shrink_line(String line_text_,boolean in_tri_quote_){
      if( line_text_.indexOf('"')==-1 &&
          line_text_.indexOf('\'')==-1 &&
          line_text_.indexOf('#')==-1 ){
         if( in_tri_quote_ ){
            if( verbose ) System.out.println("SHRINK LINE "+file_line()+"\n"+line_text_+"\n@@@EMPTY@@@");
            return "";
            }
         return line_text_;
         }
      StringBuilder _sb = new StringBuilder();
      int           _len= line_text_.length();
      boolean       _in_tri_quote= in_tri_quote_;
      boolean       _escape      = false;
      char          _quote       = 0;
      for(int _idx=0;_idx<_len;++_idx){
         char _c= line_text_.charAt(_idx);
         if( _quote!=0 ){
            if( _escape ){
               if( _c=='\\' )_escape=false;
               continue;
               }
            if( _c=='\\' ) _escape=true;
            else if( _c==_quote ){
               _sb.append(_c).append(' ');
               _quote=0;
               }
            continue;
            }
         if( _in_tri_quote ){
            if( _c=='"' && (_idx+2)<_len ){
               if(  line_text_.charAt(_idx+1)=='"' 
                  &&line_text_.charAt(_idx+2)=='"'){
                  // ３連引用符終了
                  _sb.append(TRIPLE_QUOTE).append(' ');
                  _in_tri_quote=false;
                  _idx+=2;
                  continue;
                  }
               }
            }
         if( _c=='#' ) {
            if( line_text_.substring(_idx).startsWith("#ign") ){
               ign=current_line_no;
               }
            break;
            }
         if( _c=='\'' ){
            _sb.append(_c).append(' ');
            _quote=_c;
            continue;
            }
         else if( _c=='"' ){
            if( (_idx+2)<_len ){
               if(  line_text_.charAt(_idx+1)=='"' 
                  &&line_text_.charAt(_idx+2)=='"'){
                  // ３連引用符
                  _sb.append(TRIPLE_QUOTE).append(' ');
                  _in_tri_quote=true;
                  _idx+=2;
                  continue;
                  }
               }
            _sb.append(_c).append(' ');
            _quote=_c;
            continue;
            }
         else{
            //文字列外
            _sb.append(_c);
            }
         }
      if( verbose ) System.out.println("SHRINK LINE "+file_line()+"\n"+line_text_+"\nAS:"+_sb.toString());
      return _sb.toString();
      }
   int count(String string_,char char_){
      int _count= 0;
      int _pos  = 0;
      while( (_pos=string_.indexOf(char_,_pos))!=-1 ){
         ++_count;
         _pos=_pos+1;
         }
      return _count;
      }
   int count(String text_,String elm_){
      int _count= 0;
      int _pos  = 0;
      while( (_pos=text_.indexOf(elm_,_pos))!=-1 ){
         ++_count;
         _pos=_pos+elm_.length();
         }
      return _count;
      }
   String line(){
      return String.format("%03d",current_line_no);
      }
   String file_line(){
      return current_file+":"+current_line_no;
      }
   void exception(String message_){
      if(detail) detail();
      throw new hiException(message_+" "+file_line());
      }
   void message(String message_){
      if(detail) detail();
      System.out.println(file_line()+"\t"+message_);
      }
   void detail(){
      System.out.println("\n\n"
                       +"INDENT="+indent+"\n"
                       +"SELF="+hiU.str(self_vals)
                       +(self_vals!=null?(" CLASS="+className):""));
      }
   String error(String name_,String message_){
      String _old_msg= error_out.get(name_);
      if( omit_same ){
         if( message_.equals(_old_msg) ) return null;
         error_out.put(name_,message_);
         }
      if(detail) detail();
      System.out.println(file_line()+"\t"+name_+" "+message_);
      if( ++errors>=disp_errors ) System.exit(1);
      return null;
      }
   String getType(String name_){
      String _res=null;
      if( name_.startsWith("self.") ){
         if( self_vals==null ){
            return error(name_," not in class");
            }
         _res=self_vals.get(name_);
         if( _res==null ){
            error(name_,"\tNOT FOUND");
            if( !no_tent ){
               setVarType(name_,"#tentative");
               }
            return null;
            }
         if( verbose||step ){
            System.out.println(line()+"\tAS "+className+" "+name_+" ("+_res+")");
            }
         return _res;
         }
      if( current_scope!=null ){
         _res= check_parent(name_,current_scope);
         if( _res==null ){
            if( in_def ){
               _res=check_child(name_,global);
               if( _res!=null ){
                  return error(name_,"\tASSIGN to GLOBAL ("+_res+")");
                  }
               }
            error(name_,"\tNOT FOUND");
            if( !no_tent ){
               setVarType(name_,"#tentative");
               }
            return null;
            }
         if( verbose||step ){
            System.out.println(line()+"\tFOUND "+name_+" ("+_res+")");
            }
         return _res;
         }
      else{
         if( in_class && !in_def ){
            if( verbose||step){
               System.out.println(line()+"\tCLASS "+className+" VAR "+name_);
               }
            }
         else {
            error(name_,"\tOUT OF SCOPE");
            }
         return null;
         } 
      }
   // 自分と子に含まれるかチェック
   String check_child(String name_,VarScope scope_){
      String _res= scope_.vals.get(name_);
      if( _res!=null) return _res;
      for(VarScope _ch:scope_.childs){
         _res= check_child(name_,_ch);
         if( _res!=null ) return _res;
         }
      return null;
      }
   // 自分と親に含まれるかチェック
   String check_parent(String name_,VarScope scope_){
      String _res= scope_.vals.get(name_);
      if( _res!=null) return _res;
      if( scope_.parent!=null ){
         _res= check_parent(name_,scope_.parent);
         if( _res!=null ) return _res;
         }
      return null;
      }
   // 注意！戻り値は意味を持っていない
   String setVarType(String name_,String type_){
      String _res;

      if( name_.startsWith("self.") ){
         if( self_vals==null ){
            return error(name_," not in class");
            }
         if( self_vals.containsKey(name_) ){
            return error(name_,"\tALREADY DEFINED");
            }
         if( verbose||step ){
            System.out.println(line()+"\tDECLARE "+className+" SELF "+name_+" ("+type_+")");
            }
         return self_vals.put(name_,type_);
         }
      if( current_scope!=null ){
         if( check_parent(name_,current_scope)!=null ){
            return error(name_,"\tALREADY DEFINED");
            }
         if( check_child(name_,current_scope)!=null ){
            return error(name_,"\tUSED in inner scope");
            }
         if( in_def && !type_.equals("#global") ){
            if( check_child(name_,global)!=null ){
                return error(name_,"\tREDEFINE of GLOBAL");
                }
            }
         if( verbose||step ){
            System.out.println(line()+"\tDECLARE "+name_+" ("+type_+")");
            }
         current_scope.vals.put(name_,type_);
         return name_;
         }
      if( in_class && !in_def ){
         if(verbose||step){
            System.out.println(line()+"\tCLASS "+className+" VAR "+name_);
            }
         }
      else {
         return error(name_,"\tOUT OF SCOPE");
         }
      return name_;
      }
   public static void main(String[] argv_)throws Exception{
      hiPyCheck _this  =new hiPyCheck(argv_);
      int       _errors=_this.start();
      System.exit(_errors);
      }
   }
